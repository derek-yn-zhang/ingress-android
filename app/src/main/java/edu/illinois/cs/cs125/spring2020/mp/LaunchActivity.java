package edu.illinois.cs.cs125.spring2020.mp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

/**
 * This class overrides MainActivity, taking priority when the app is launched.
 */
public class LaunchActivity extends AppCompatActivity {

    /** Invited to the game but has not yet responded to the invitation. */
    public static final int RC_SIGN_IN = 5;

    /**
     * This ensures that the user is logged in before playing the game.
     * @param savedInstanceState - saved states
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // provided logic
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // launch MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // start login activity for result - see below discussion
            this.createSignInIntent();
        }
    }

    /**
     * This creates sign-in intents as per the google example.
     */
    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.EmailBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    /**
     * Getting results from the Activity section.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // launch MainActivity
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response == null) {
                // user canceled
                    TextView goLogin = findViewById(R.id.goLogin);
                    goLogin.setVisibility(View.VISIBLE);
                    goLogin.setOnClickListener(v -> {
                        // recurse on createSignInIntent
                        this.createSignInIntent();
                    });
                } else {
                    response.getError().getErrorCode();
                }
            }
        }
    }
}
