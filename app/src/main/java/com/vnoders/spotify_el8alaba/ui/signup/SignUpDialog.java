package com.vnoders.spotify_el8alaba.ui.signup;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.vnoders.spotify_el8alaba.R;

/**
 *
 */
public class SignUpDialog extends DialogFragment {

    private Button ok_button;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_signup, container, false);
        ok_button = view.findViewById(R.id.oK);

        ok_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;


    }
}
