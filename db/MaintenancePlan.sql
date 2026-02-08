-- Plan de mantenimiento para respaldos diarios de cv_ventas_distribucion
/* Iniciar el Agente SQL Server antes de ejecutar este script */
-- Crea un trabajo de SQL Agent programado a las 21:00 cada día.

USE msdb;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Parámetros
DECLARE @backupPath NVARCHAR(260) = N'D:\OneDrive\bd_backups\Comercials_Valerio';
DECLARE @jobName NVARCHAR(128) = N'CV_Backup_Daily';
DECLARE @scheduleName NVARCHAR(128) = N'CV_Backup_Daily_21_00';

-- Elimina el trabajo previo si existe
IF EXISTS (SELECT 1 FROM msdb.dbo.sysjobs WHERE name = @jobName)
BEGIN
    EXEC sp_delete_job @job_name = @jobName;
END;

-- Crea el trabajo
EXEC sp_add_job
    @job_name = @jobName,
    @enabled = 1,
    @description = N'Full backup of cv_ventas_distribucion executed daily at 21:00';

-- Paso del trabajo: realiza copia completa y elimina archivos de más de 30 días
EXEC sp_add_jobstep
    @job_name = @jobName,
    @step_name = N'Backup database',
    @subsystem = N'TSQL',
    @command = N'
DECLARE @backupPath NVARCHAR(260) = N''D:\OneDrive\bd_backups\Comercials_Valerio'';
BEGIN TRY
    DECLARE @dt NVARCHAR(20) = CONVERT(VARCHAR(8), SYSDATETIME(), 112) + ''_'' +
        REPLACE(CONVERT(VARCHAR(8), SYSDATETIME(), 108), '':'', '''');
    DECLARE @file NVARCHAR(260) = @backupPath + ''\cv_ventas_'' + @dt + ''.bak'';

    -- Crea la carpeta solo si no existe
    DECLARE @dirExists INT;
    EXEC master.dbo.xp_fileexist @backupPath, @dirExists OUTPUT;
    IF @dirExists = 0
    BEGIN
        EXEC master.dbo.xp_create_subdir @backupPath;
        IF @@ERROR <> 0
            RAISERROR(''Failed to create directory %s'', 16, 1, @backupPath);
    END;

    BACKUP DATABASE cv_ventas_distribucion
        TO DISK = @file
        WITH INIT, COMPRESSION, STATS = 10;
    IF @@ERROR <> 0
        RAISERROR(''Database backup failed to %s'', 16, 1, @file);

    DECLARE @old DATETIME = DATEADD(DAY, -30, GETDATE());
    EXEC master.dbo.xp_delete_file 0, @backupPath, ''bak'', @old;
    IF @@ERROR <> 0
        RAISERROR(''Failed to delete old backups from %s'', 16, 1, @backupPath);
END TRY
BEGIN CATCH
    DECLARE @msg NVARCHAR(2048) = ERROR_MESSAGE();
    RAISERROR(''Backup job step error: %s'', 16, 1, @msg);
END CATCH;
',
    @database_name = N'master';

-- Crear horario diario a las 21:00
IF EXISTS (SELECT 1 FROM msdb.dbo.sysschedules WHERE name = @scheduleName)
    EXEC msdb.dbo.sp_delete_schedule @schedule_name = @scheduleName;
EXEC sp_add_schedule
    @schedule_name = @scheduleName,
    @freq_type = 4,            -- daily
    @freq_interval = 1,
    @active_start_time = 210000;  -- 21:00

-- Asociar el horario al trabajo
EXEC sp_attach_schedule
    @job_name = @jobName,
    @schedule_name = @scheduleName;

-- Agregar el trabajo al servidor
EXEC sp_add_jobserver @job_name = @jobName;
GO
