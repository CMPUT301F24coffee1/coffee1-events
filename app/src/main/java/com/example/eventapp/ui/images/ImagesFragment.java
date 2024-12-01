package com.example.eventapp.ui.images;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import java.util.Arrays;
import java.util.List;
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
    private ArrayList<String> eventImageUriStrings;
    private ArrayList<String> profileImageUriStrings;
    private ArrayList<String> facilityImageUriStrings;
    private ImageAdapter eventImagesAdapter;
    private ImageAdapter profileImagesAdapter;
    private ImageAdapter facilityImagesAdapter;
    ImageListAdapter imagesListAdapter;
    ViewPager2 viewPager;

    private FragmentAdminImagesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
        binding = FragmentAdminImagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // set up RecyclerView for event images
        eventImageUriStrings = new ArrayList<>();
        eventImagesAdapter = new ImageAdapter(eventImageUriStrings, this);

        // set up RecyclerView for profile images
        profileImageUriStrings = new ArrayList<>();
        profileImagesAdapter = new ImageAdapter(profileImageUriStrings, this);

        // set up RecyclerView for facility images
        facilityImageUriStrings = new ArrayList<>();
        facilityImagesAdapter = new ImageAdapter(facilityImageUriStrings, this);

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

    private void updateImagesList(String type, List<StorageReference> newEventImageUriStrings) {
        AtomicReference<ArrayList<String>> uriStringList = new AtomicReference<>();
        AtomicReference<ImageAdapter> imageAdapter = new AtomicReference<>();
        switch (type) {
            case "events":
                uriStringList.set(eventImageUriStrings);
                imageAdapter.set(eventImagesAdapter);
                break;
            case "profiles":
                uriStringList.set(profileImageUriStrings);
                imageAdapter.set(profileImagesAdapter);
                break;
            case "facilities":
                uriStringList.set(facilityImageUriStrings);
                imageAdapter.set(facilityImagesAdapter);
                break;
            default:
                throw new IllegalStateException("Unexpected image type: " + type);
        }
        uriStringList.get().clear();
        for (StorageReference imagePrefixUri : newEventImageUriStrings) {
            // Image is stored as image inside of current uri directory, so we have to fetch that
            imagePrefixUri.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference image : listResult.getItems()) {
                        // For loop, but it should only be one item
                        image.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Log.i(TAG, "Successfully found image " + image);
                                uriStringList.get().add(uri.toString());
                                imageAdapter.get().notifyDataSetChanged();
                            })
                            .addOnFailureListener(throwable -> {
                                Log.e(TAG, "Image " + image + " failed to get downloadable url.");
                            });
                    }

                })
                .addOnFailureListener(throwable -> {
                    Log.e(TAG, "Image with prefix " + imagePrefixUri + " failed to find internal image.", throwable);
                });

        }
    }

    @Override
    public void onImageClick(String imageUriString) {
        Log.d("ImagesFragment", "Image clicked");
        showImageInfoPopup(imageUriString);
    }

    private void showImageInfoPopup(String imageUriString) {
        // todo
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}