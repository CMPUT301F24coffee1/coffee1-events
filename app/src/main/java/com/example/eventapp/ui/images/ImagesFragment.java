package com.example.eventapp.ui.images;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentAdminImagesBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The ImagesFragment class displays all of the images in the app, and sorts them into three categories:
 * Events, all images belonging to events,
 * Profiles, all images belonging to profiles, and
 * Facilities, all images belonging to facilities.
 * This fragment uses the ImagesViewModel, which accesses the storage database and gives the information
 * to populate the lists of images with. It can also delete the images from their respective origins.
 */
public class ImagesFragment extends Fragment implements
        ImageAdapter.OnImageClickListener {

    private ImagesViewModel imagesViewModel;
    private ArrayList<Uri> eventImageUris;
    private ArrayList<Uri> profileImageUris;
    private ArrayList<Uri> facilityImageUris;
    private ImageAdapter eventImagesAdapter;
    private ImageAdapter profileImagesAdapter;
    private ImageAdapter facilityImagesAdapter;
    ImageListAdapter imagesListAdapter;
    ViewPager2 viewPager;

    private FragmentAdminImagesBinding binding;

    /**
     * Assigns the binding to the Admin Images Fragment binding, and assigns the ImagesViewModel
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root of the Admin View Images fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
        binding = FragmentAdminImagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Create the lists and adapters, and populates them with all of the images in the database
     * by using the ViewModel
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // set up RecyclerView for event images
        eventImageUris = new ArrayList<>();
        eventImagesAdapter = new ImageAdapter(eventImageUris, this);

        // set up RecyclerView for profile images
        profileImageUris = new ArrayList<>();
        profileImagesAdapter = new ImageAdapter(profileImageUris, this);

        // set up RecyclerView for facility images
        facilityImageUris = new ArrayList<>();
        facilityImagesAdapter = new ImageAdapter(facilityImageUris, this);

        imagesListAdapter = new ImageListAdapter(eventImagesAdapter, profileImagesAdapter, facilityImagesAdapter);
        viewPager = view.findViewById(R.id.images_viewpager);
        viewPager.setAdapter(imagesListAdapter);

        String[] imageTypes = new String[]{"events", "profiles", "facilities"};
        for (String type : imageTypes) {
            imagesViewModel.getImages(type).thenAccept(images -> {
                Log.i(TAG, "Successfully received all listed " + type + " images");
                updateImagesList(type, images);
            }).exceptionally(throwable -> {
                Log.e(TAG, "Failed to receive listed " + type + " images", throwable);
                return null;
            });
        }

        TabLayout tabLayout = view.findViewById(R.id.images_tabs);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(
                        position == 0 ? getString(R.string.title_events) :
                                position == 1 ? getString(R.string.title_profiles) :
                                        getString(R.string.title_facilities)
                ))
                .attach();

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                if (!user.isAdmin()) {
                    // Kick out user if they're not supposed to be here
                    NavHostFragment.findNavController(this).popBackStack();
                }
            }
        });

    }

    /**
     * Updates the images lists and adapters to have new information, run after the View Model
     * finishes finding the relevant directories
     * @param type The type of image to populate
     * @param newImageRefs The new directory where the images will be found to populate with
     */
    private void updateImagesList(String type, List<StorageReference> newImageRefs) {
        AtomicReference<ArrayList<Uri>> uriStringList = new AtomicReference<>();
        AtomicReference<ImageAdapter> imageAdapter = new AtomicReference<>();
        switch (type) {
            case "events":
                uriStringList.set(eventImageUris);
                imageAdapter.set(eventImagesAdapter);
                break;
            case "profiles":
                uriStringList.set(profileImageUris);
                imageAdapter.set(profileImagesAdapter);
                break;
            case "facilities":
                uriStringList.set(facilityImageUris);
                imageAdapter.set(facilityImagesAdapter);
                break;
            default:
                throw new IllegalStateException("Unexpected image type: " + type);
        }
        uriStringList.get().clear();
        for (StorageReference imagePrefixUri : newImageRefs) {
            // Image is stored as image inside of current uri directory, so we have to fetch that
            imagePrefixUri.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference image : listResult.getItems()) {
                        // For loop, but it should only be one item
                        image.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Log.i(TAG, "Successfully found image " + image);
                                uriStringList.get().add(uri);
                                imageAdapter.get().notifyDataSetChanged();
                            })
                            .addOnFailureListener(throwable -> Log.e(TAG, "Image " + image + " failed to get downloadable url."));
                    }

                })
                .addOnFailureListener(throwable -> Log.e(TAG, "Image with prefix " + imagePrefixUri + " failed to find internal image.", throwable));

        }
    }

    /**
     * Handles clicking of an individual image, simply passes the selected imageUri to the showImageInfoPopup function
     * @param imageUri The Uri of the currently selected image
     */
    @Override
    public void onImageClick(Uri imageUri) {
        Log.d(TAG, "Image clicked with uri: " + imageUri);
        showImageInfoPopup(imageUri);
    }

    /**
     * Finds the object associated with the selected image, if it exists, and then displays the
     * popup that shows the image, putting the selected image into the View Model
     * @param imageUri The image Uri to select
     */
    private void showImageInfoPopup(Uri imageUri) {
        ImageInfoFragment imageInfoFragment = new ImageInfoFragment();
        FragmentManager.FragmentLifecycleCallbacks callback = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                if (imagesViewModel.getImageRemoved()) {
                    switch (Objects.requireNonNull(imageUri.getLastPathSegment()).split("/")[0]) {
                        case "events":
                            eventImageUris.remove(imageUri);
                            eventImagesAdapter.notifyDataSetChanged();
                            break;
                        case "profiles":
                            profileImageUris.remove(imageUri);
                            profileImagesAdapter.notifyDataSetChanged();
                            break;
                        case "facilities":
                            facilityImageUris.remove(imageUri);
                            facilityImagesAdapter.notifyDataSetChanged();
                            break;
                    }
                }
                super.onFragmentDestroyed(fm, f);

            }
        };
        requireActivity().getSupportFragmentManager().registerFragmentLifecycleCallbacks(callback, true);
        imagesViewModel.setSelectedImage(imageUri);
        imagesViewModel.setSelectedObject(imageUri).thenAccept(foundObject -> {
            imageInfoFragment.show(requireActivity().getSupportFragmentManager(), "fragment_image_info");
            if (!foundObject) {
                Log.w(TAG, "Displaying image that does not have an associated object for Image Uri: " + imageUri);
            }
        });

    }

    /**
     * Removes the binding when the view is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}