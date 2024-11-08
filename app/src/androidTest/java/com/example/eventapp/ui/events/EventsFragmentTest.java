// Temporarily doesn't work due to user not showing create event if they're not an organizer
// Must be fixed by mocking a user

//package com.example.eventapp.ui.events;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.filters.LargeTest;
//
//import com.example.eventapp.MainActivity;
//import com.example.eventapp.R;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//@LargeTest
//public class EventsFragmentTest {
//
//    @Before
//    public void setUp() {
//        // Launch MainActivity where EventsFragment is hosted
//        ActivityScenario.launch(MainActivity.class);
//    }
//
//    @Test
//    public void testShowCreateEventPopup() {
//        // Click on the create event button
//        onView(withId(R.id.create_event_button)).perform(click());
//
//        // Check if the CreateEventFragment is displayed by looking for a unique element in it
//        onView(withId(R.id.popup_create_event_button)).check(matches(isDisplayed()));
//    }
//
//}
