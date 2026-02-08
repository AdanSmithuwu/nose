package com.comercialvalerio.presentation.controller.auth;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.application.exception.AuthenticationException;
import com.comercialvalerio.presentation.ui.auth.LoginForm;
import com.comercialvalerio.presentation.core.AsyncTasks;
import javax.swing.SwingUtilities;
import jakarta.ws.rs.WebApplicationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Lógica para el LoginDialog.
 */
public final class LoginController {

    private final LoginForm view;
    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    public LoginController(LoginForm frm){
        this.view = frm;
    }

    /* ---------------------------------------------------------------- */
    public void onLogin(){
        String usr = view.getUsuario();
        String pwd = view.getPassword();

        if (usr.isBlank() || pwd.isBlank()){
            raven.toast.Notifications.getInstance()
                                    .show(raven.toast.Notifications.Type.ERROR,
                                          "Ingrese usuario y contraseña");
            return;
        }

        AsyncTasks.busy(view, () -> {
            try {
                EmpleadoDto emp = UiContext.empleadoSvc().autenticar(usr, pwd);
                UiContext.setUsuarioActual(emp);
                return emp;
            } catch (AuthenticationException ex) {
                LOG.log(Level.INFO, "Login failed: {0}", ex.getMessage());
                SwingUtilities.invokeLater(() ->
                        raven.toast.Notifications.getInstance()
                                .show(raven.toast.Notifications.Type.ERROR,
                                      ErrorHandler.resolveMessage(ex)));
                view.clearPassword();
                return null;
            } catch (WebApplicationException ex) {
                LOG.log(Level.WARNING, "Error al autenticar", ex);
                String msg = ErrorHandler.resolveMessage(ex);
                SwingUtilities.invokeLater(() ->
                        raven.toast.Notifications.getInstance()
                                .show(raven.toast.Notifications.Type.ERROR,
                                      msg));
                view.clearPassword();
                return null;
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al autenticar", ex);
                String msg = ErrorHandler.resolveMessage(ex);
                SwingUtilities.invokeLater(() ->
                        raven.toast.Notifications.getInstance()
                                .show(raven.toast.Notifications.Type.ERROR,
                                      msg));
                view.clearPassword();
                return null;
            }
        }, emp -> {
            if (emp != null) {
                view.clearCredentials();
                view.markAuthenticated();
                raven.toast.Notifications.getInstance()
                        .show(raven.toast.Notifications.Type.SUCCESS,
                              "Acceso concedido");
            }
        });
    }
}
