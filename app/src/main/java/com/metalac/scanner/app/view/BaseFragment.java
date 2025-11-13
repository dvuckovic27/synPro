package com.metalac.scanner.app.view;

import static com.metalac.scanner.app.utils.Utils.SHOW_KEYBOARD_DELAY;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment extends Fragment {

    private ViewBinding mViewBinding;
    private BasePresenter mPresenter;
    private boolean isFirstKeyboardShow = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = onBindLayout();
        BaseView baseView = onBindContract();
        View view = mViewBinding.getRoot().getRootView();
        mPresenter = onBindPresenter();
        if (mPresenter != null) mPresenter.onAttach(baseView);

        MenuProvider provider = provideMenuProvider();
        if (provider != null) {
            requireActivity().addMenuProvider(provider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        }

        onCreateView(mViewBinding, mPresenter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewBinding = null;
        if (mPresenter != null) {
            mPresenter.onDetach();
        }
    }

    @Nullable
    protected MenuProvider provideMenuProvider() {
        return null;
    }

    protected abstract void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter);

    protected abstract ViewBinding onBindLayout();

    protected abstract BaseView onBindContract();

    protected abstract BasePresenter onBindPresenter();

    /**
     * Navigates from the current destination to the specified next destination using the Navigation component.
     * <p>
     * This method checks if the current destination matches the provided {@code currentDestinationId}
     * before performing navigation. If {@code args} is not null, they are passed along with the navigation action.
     *
     * @param view                 The view used to find the {@link NavController}. Must not be null.
     * @param currentDestinationId The expected current destination ID to validate before navigation.
     * @param nextDestinationId    The destination ID to navigate to.
     * @param args                 Optional navigation arguments; can be null.
     */
    public void navigate(View view, int currentDestinationId, int nextDestinationId, @Nullable Bundle args) {
        if (view == null) return;

        NavController navController;
        try {
            navController = Navigation.findNavController(view);
        } catch (Exception e) {
            return;
        }

        NavDestination current = navController.getCurrentDestination();
        if (current == null || current.getId() != currentDestinationId) return;

        if (args == null) {
            navController.navigate(nextDestinationId);
        } else {
            navController.navigate(nextDestinationId, args);
        }
    }

    /**
     * Toggles the visibility of the soft keyboard for the given {@link View}, typically an {@link EditText}.
     * <p>
     * When {@code show} is {@code true}, the method requests focus on the view and shows the keyboard.
     * If the view is an instance of {@link EditText}, the cursor is automatically placed at the end of the text.
     * If {@code isFirstKeyboardShow} is {@code true}, it introduces a delay (defined by {@code SHOW_KEYBOARD_DELAY})
     * before showing the keyboard to ensure proper UI rendering. Subsequent calls use immediate posting.
     * </p>
     * <p>
     * When {@code show} is {@code false}, the method hides the keyboard and clears focus from the view.
     * </p>
     *
     * @param view the target view to show/hide the keyboard for (e.g., an EditText). Can be null.
     * @param show {@code true} to show the keyboard; {@code false} to hide it.
     */
    public void toggleKeyboard(@Nullable View view, boolean show) {
        if (view == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        if (show) {
            Runnable showRunnable = () -> {
                if (view.requestFocus()) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        editText.setSelection(editText.getText().length());
                    }
                }
            };

            if (isFirstKeyboardShow) {
                view.postDelayed(showRunnable, SHOW_KEYBOARD_DELAY);
                isFirstKeyboardShow = false;
            } else {
                view.post(showRunnable);
            }
        } else {
            view.clearFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
