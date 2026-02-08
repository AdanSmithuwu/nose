package com.comercialvalerio.presentation.ui.main.dependencies;

/**
 *
 */
public class MenuAction {

    protected boolean isCancel() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }

    private boolean cancel = false;
}
