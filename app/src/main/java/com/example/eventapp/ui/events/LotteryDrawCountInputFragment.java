package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventapp.R;

/**
 * Used to prompt the user to input how many entrants on the waiting list they want to draw
 * (for the invitation to enroll.)
 */
public class LotteryDrawCountInputFragment extends DialogFragment {
    private final LotteryDrawCountInputListener listener;
    private final int enrollSpaceRemaining;

    interface LotteryDrawCountInputListener {
        void lotteryDraw(int drawCount);
    }

    public LotteryDrawCountInputFragment(LotteryDrawCountInputListener listener, int enrollSpaceRemaining){
        this.listener = listener;
        this.enrollSpaceRemaining = enrollSpaceRemaining;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        View view = getLayoutInflater().inflate(R.layout.fragment_lottery_draw_count_input, null);
        EditText drawCountInputBox = view.findViewById(R.id.fragment_lottery_draw_count_input_box);
        drawCountInputBox.setHint("Draw Count (<="+this.enrollSpaceRemaining+")");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder.setView(view).setTitle("Lottery Draw Count").setNegativeButton("Cancel", null)
                .setPositiveButton("Draw", (dialogInterface, i) -> {
                    String drawCountInput = drawCountInputBox.getText().toString();
                    if(drawCountInput.isEmpty()){
                        //show toast
                        Toast.makeText(getContext(), "Draw Count is Empty", Toast.LENGTH_SHORT).show();
                    }else{
                        int chosenDrawCount = Integer.parseInt(drawCountInput);

                        if(chosenDrawCount > this.enrollSpaceRemaining) {
                            Toast.makeText(getContext(), "Draw Count is too Large", Toast.LENGTH_SHORT).show();
                        } else if(chosenDrawCount < 1) {
                            Toast.makeText(getContext(), "Draw Count is less than 1", Toast.LENGTH_SHORT).show();
                        }else{
                            listener.lotteryDraw(chosenDrawCount);
                        }

                    }
                }).create();
    }
}
