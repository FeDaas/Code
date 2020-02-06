package de.moviemanager.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.ui.dialog.SimpleDialog;
import de.moviemanager.ui.masterfragments.SearchMasterFragment;
import de.moviemanager.ui.masterfragments.onetimetask.OneTimeTaskExecutor;
import de.moviemanager.ui.masterfragments.onetimetask.OneTimeTaskExecutorRudiment;
import de.storage.StorageException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static de.moviemanager.ui.masterfragments.PortrayableMasterFragment.newMovieFragmentInstance;
import static de.moviemanager.ui.masterfragments.PortrayableMasterFragment.newPerformerFragmentInstance;

public class MasterActivity
        extends ResultHandlingActivity
        implements OneTimeTaskExecutorRudiment {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    public static final String STORAGE_NAME = "movie_manager";
    public static final String FRAGMENT_NAME = "fragment_name";

    private static final int WRITE_REQUEST_CODE = 0;

    private final OneTimeTaskExecutor oneTimeTasks = new OneTimeTaskExecutor();
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        checkPermissionsState();
    }

    private void checkPermissionsState() {
        if (STORAGE.areRequiredPermissionsGrantedFor(this)) {
            setupIfPermissionIsGranted();
        } else {
            requestPermissions(STORAGE.getRequiredPermissions(), WRITE_REQUEST_CODE);
        }
    }

    private void setupIfPermissionIsGranted() {
        initStorage();

        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.bottom_navigation_movies);
        openInitialFragment();
        navigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void initStorage() {
        try {
            STORAGE.openMovieManagerStorage();
        } catch(StorageException exception) {
            SimpleDialog.error(this)
                    .setMessage(R.string.error_storage_message)
                    .setPositiveButtonText(R.string.Ok)
                    .setPositiveButtonAction(frag -> finish())
                    .show();
        }
    }

    private void openInitialFragment() {
        final Fragment fragment = createMoviesFragment();
        openFragment(fragment, false);
    }

    private Fragment createMoviesFragment() {
        final List<Movie> movies = STORAGE.getMovies();
        int menuId = R.string.bottom_navigation_menu_movies;
        return newMovieFragmentInstance(menuId, movies);
    }

    private boolean onNavigationItemSelected(final MenuItem menuItem) {
        Fragment fragment = null;
        int itemId = menuItem.getItemId();

        if (itemId == R.id.bottom_navigation_movies) {
            fragment = createMoviesFragment();
        } else if (itemId == R.id.bottom_navigation_performers) {
            fragment = createPerformersFragment();
        } else if (itemId == R.id.bottom_navigation_search) {
            fragment = createSearchFragment();
        }

        if(fragment != null) {
            openFragment(fragment);
        }

        return fragment != null;
    }

    private Fragment createPerformersFragment() {
        final List<Performer> performers = STORAGE.getPerformers();
        int menuId = R.string.bottom_navigation_menu_performers;
        return newPerformerFragmentInstance(menuId, performers);
    }

    private Fragment createSearchFragment() {
        int menuId = R.string.bottom_navigation_menu_search;
        return SearchMasterFragment.newInstance(menuId);
    }

    private void openFragment(final Fragment fragment) {
        openFragment(fragment, true);
    }

    private void openFragment(final Fragment fragment, boolean addToBackStack) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        if(addToBackStack) {
            transaction.addToBackStack(null);
        } else {
            transaction.disallowAddToBackStack();
        }
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == WRITE_REQUEST_CODE) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                STORAGE.updateRequiredPermissions(this);
                setupIfPermissionIsGranted();
            } else {
                finish();
            }
        }
    }

    public void setBottomNavigationTo(final @StringRes int nameId) {
        navigationView.setOnNavigationItemSelectedListener(null);
        switch (nameId) {
            case R.string.bottom_navigation_menu_movies:
                navigationView.setSelectedItemId(R.id.bottom_navigation_movies);
                break;
            case R.string.bottom_navigation_menu_performers:
                navigationView.setSelectedItemId(R.id.bottom_navigation_performers);
                break;
            case R.string.bottom_navigation_menu_search:
                navigationView.setSelectedItemId(R.id.bottom_navigation_search);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + nameId);
        }
        navigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    public void addOneTimeTask(Runnable r) {
        oneTimeTasks.addOneTimeTask(r);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            runTasksOnceAndClear();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void runTasksOnceAndClear() {
        oneTimeTasks.runTasksOnceAndClear();
    }
}
