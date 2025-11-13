package com.metalac.scanner.app.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.helpers.ScannerHelper;
import com.metalac.scanner.app.helpers.ScannerHelperFactory;
import com.metalac.scanner.app.databinding.ActivityMainBinding;
import com.metalac.scanner.app.models.ScanResult;
import com.metalac.scanner.app.models.WeightBarcode;
import com.metalac.scanner.app.view.interfaces.IOnScanCallback;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;
    private IOnScanCallback mOnScanCallback;
    private final ScannerHelper mScannerHelper = ScannerHelperFactory.getScannerHelper();
    private final BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleScan(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for scan result broadcast
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mScanReceiver, mScannerHelper.getScanIntentFilter(), RECEIVER_EXPORTED);
        } else {
            registerReceiver(mScanReceiver, mScannerHelper.getScanIntentFilter());
        }

        // Configure scanner
        mScannerHelper.configureScanner(this);

        // Your existing UI setup methods
        setupEdgeToEdge();
        setupViewBinding();
        setupToolbar();
        setupWindowInsets();
        setupNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScanReceiver);
        mOnScanCallback = null;
    }

    /**
     * Handles navigation when the "Up" button in the toolbar is pressed.
     * <p>
     * Delegates the navigation action to the NavController, falling back to default behavior if needed.
     *
     * @return {@code true} if navigation was handled successfully, {@code false} otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fcvNavHost);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void setOnScanCallback(IOnScanCallback onScanCallback) {
        this.mOnScanCallback = onScanCallback;
    }

    public void removeOnScanCallback() {
        this.mOnScanCallback = null;
    }

    /**
     * Enables edge-to-edge display by configuring window decor fitting.
     */
    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
    }

    /**
     * Inflates the view binding for the main activity and sets the root view.
     */
    private void setupViewBinding() {
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }

    /**
     * Configures the toolbar with overflow icon and assigns it as the support action bar.
     */
    private void setupToolbar() {
        setSupportActionBar(mBinding.toolbar);
    }

    /**
     * Sets the overflow icon of the toolbar.
     *
     * @param drawable The resource ID of the drawable to set as the overflow icon.
     */
    public void setupToolbarIcon(int drawable) {
        mBinding.toolbar.setOverflowIcon(ContextCompat.getDrawable(this, drawable));
    }

    /**
     * Applies window insets (status bar, navigation bar, and IME) to ensure
     * proper padding and appearance adjustments for edge-to-edge content.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.toolbar.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }

        int currentBottomPadding = mBinding.getRoot().getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemInsets.bottom, imeInsets.bottom);
            mBinding.toolbar.setPadding(mBinding.toolbar.getPaddingLeft(), systemInsets.top, mBinding.toolbar.getPaddingRight(), mBinding.toolbar.getPaddingBottom());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), currentBottomPadding + bottom);
            return insets;
        });
    }

    /**
     * Configures the toolbar for the specified navigation destination.
     * <p>
     * Sets the toolbar title and home/up indicator visibility and icon based on the destination.
     *
     * @param destinationId The ID of the current navigation destination.
     * @param title         The title to display on the toolbar.
     */
    public void setupToolbar(int destinationId, String title) {
        setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        if (destinationId == R.id.MenuFragment || destinationId == R.id.ConfigurationFragment) {
            actionBar.setHomeAsUpIndicator(null);
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Initializes the navigation graph and sets the start destination based on
     * whether the store code is set in preferences.
     * <p>
     * If the store code is set, navigation starts at {@code MenuFragment}.
     * Otherwise, it starts at {@code ConfigurationFragment}.
     * </p>
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fcvNavHost);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

            int startDest = PrefManager.isStoreCodeSet()
                    ? R.id.MenuFragment
                    : R.id.ConfigurationFragment;

            navGraph.setStartDestination(startDest);
            navController.setGraph(navGraph);
        }
    }


    /**
     * Handles a barcode scan intent by parsing the scan result, determining its type,
     * and invoking the appropriate callback method.
     * <p>
     * This method normalizes the barcode type string so it can match EAN-13 barcodes
     * across different scanner manufacturers (e.g., Zebra, PM84), which may return
     * slightly different formats such as:
     * <ul>
     *   <li>"LABEL-TYPE-EAN13"</li>
     *   <li>"EAN 13"</li>
     *   <li>"ean13"</li>
     *   <li>"EAN-13"</li>
     * </ul>
     * All spaces and dashes are removed, and matching is done case-insensitively.
     * <p>
     * If the scanned code is an EAN-13 and starts with {@link WeightBarcode#PREFIX},
     * it is treated as a weight barcode and passed to
     * {@link IOnScanCallback#onWeightBarcodeScanResult(String)}.
     * Otherwise, it is passed to
     * {@link IOnScanCallback#onBarcodeScanResult(String)}.
     *
     * @param intent The intent containing the scan result from the scanner.
     */
    public void handleScan(Intent intent) {
        ScanResult result = mScannerHelper.parseScanIntent(intent);
        if (result == null || !result.isValid() || mOnScanCallback == null) return;

        String type = result.type != null ? result.type.toUpperCase().replace("-", "").replace(" ", "") : "";
        String data = result.data != null ? result.data : "";

        boolean isEan13 = type.contains("EAN13");
        boolean isWeightBarcode = isEan13 && data.startsWith(WeightBarcode.PREFIX);

        if (isWeightBarcode) {
            mOnScanCallback.onWeightBarcodeScanResult(data);
        } else {
            mOnScanCallback.onBarcodeScanResult(data);
        }
    }

}