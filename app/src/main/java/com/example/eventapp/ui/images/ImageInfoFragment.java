package com.example.eventapp.ui.images;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.databinding.ImageInfoPopupBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/**
 * ImageInfoFragment is a fragment that displays an image, given by the ImagesViewModel.
 * Admins will see the type of image, the name of the object that the image belongs to (if it exists),
 * and a delete button to delete the image. Deleting the image will delete any reference to the image.
 */
public class ImageInfoFragment extends DialogFragment {

    private ImageInfoPopupBinding binding;
    ImagesViewModel imagesViewModel;

    /**
     * Creates and populates the image info fragment, populating it with the selected image
     * information from the ViewModel
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Root of the fragment
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = ImageInfoPopupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                if (!user.isAdmin()) {
                    // Kick out user if they're not supposed to be here
                    requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                }
            }
        });

        return root;
    }

    /**
     * Populates the fragment with the correct information of the selected image from the View Model.
     * In particular, it sets the image, and then specifies the image type, and what object it belongs to, if any.
     * Also sets listeners to handle the close buo,on adn the remove button, which removes the photo,
     * and removes the connection in the object it is related to, if it exists.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = requireDialog().getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        final FloatingActionButton closeButton = binding.imageInfoCloseButton;
        final ImageView image = binding.imageInfoImage;
        final TextView typeText = binding.imageInfoTypeText;
        final TextView nameText = binding.imageInfoNameText;
        final Button deleteButton = binding.imageInfoDelete;

        final Uri imageUri = imagesViewModel.getSelectedImage();
        String imageType;
        switch (Objects.requireNonNull(imageUri.getLastPathSegment()).split("/")[0]) {
            case "events":
                imageType = "Event";
                break;
            case "profiles":
                imageType = "Profile";
                break;
            case "facilities":
                imageType = "Facility";
                break;
            default:
                imageType = "Invalid Type";
                break;
        }

        final String name = imagesViewModel.getSelectedName();

        closeButton.setOnClickListener((v) -> requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit());

        Glide.with(requireContext())
                .load(imageUri)
                .into(image);

        typeText.setText(imageType);
        nameText.setText(name);

        deleteButton.setOnClickListener((v) -> {
            imagesViewModel.removeSelectedImage();
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });
    }

    /**
     * Makes sure to clear the binding
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
