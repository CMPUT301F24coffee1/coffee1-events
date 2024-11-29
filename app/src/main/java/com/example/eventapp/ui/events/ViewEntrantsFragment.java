package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;

import java.util.ArrayList;

public class ViewEntrantsFragment extends Fragment {

    private RecyclerView entrantsList;
    private ArrayList<User> entrants;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_view_entrants, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d("ViewEntrantsFragment", "created");
        entrantsList = view.findViewById(R.id.fragment_view_entrants_entrant_list);
        entrantsList.setLayoutManager(new LinearLayoutManager(getContext()));
        entrants = new ArrayList<>();

        // for testing
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        User actualUser = UserRepository.getInstance().getCurrentUserLiveData().getValue();
        entrants.add(actualUser);
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));

        EntrantsAdapter entrantsAdapter = new EntrantsAdapter(entrants);
        entrantsList.setAdapter(entrantsAdapter);



    }
}
