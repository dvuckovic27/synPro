package com.metalac.scanner.app.view;

import androidx.annotation.UiThread;

@UiThread
public interface BaseView {
    void showProgress();

    void hideProgress();
}
