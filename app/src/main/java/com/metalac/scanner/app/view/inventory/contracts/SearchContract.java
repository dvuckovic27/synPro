package com.metalac.scanner.app.view.inventory.contracts;

import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;

import java.util.ArrayList;

public interface SearchContract {

    interface View extends BaseView {
        void onUnitOfMeasureLoaded(ArrayList<String> unitOfMeasuresList);
        void onUnitOfMeasureFailed();
    }

    interface Presenter extends BasePresenter {
        void getUnitOfMeasure();
    }
}
