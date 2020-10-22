package com.synacor.cloudidkeycloaklogin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;
import com.synacor.cloudidkeycloaklogin.rx.RxActivity;
import com.synacor.cloudidkeycloaklogin.rx.RxKeycloak;

public class LoginActivity extends AppCompatActivity {

	// Arbitrary Request Code for capturing messages sent to this activity:
	private static int AUTH_REQUEST_CODE  = 2001;

	// Holds stream instances for disposal:
	private final CompositeDisposable mDestroyDisposeBag = new CompositeDisposable();

	// Stream subject for onActivityResult method:
	private PublishSubject<RxActivity.ActivityResult> mOnActivityResult = PublishSubject.create();

	// Internal cached instance of the Keycloak config:
	private RxKeycloak.Config mKeycloakConfig;

	/**
	 * @return Generate and return a default keycloak config object drawing values
	 * from resource file: keycloak-config.
	 */
	private RxKeycloak.Config getDefaultConfig() {
		if(mKeycloakConfig == null) {
			mKeycloakConfig =  new RxKeycloak.Config(
				getString(R.string.authServerUrl),
				getString(R.string.realm),
				getString(R.string.clientId),
				"appauth://oauth2redirect"
			);
		}
		return mKeycloakConfig;
	}

	/**
	 * Apply the supplied config to the UI, populating all form fields.
	 */
	private void applyConfigToUI(final RxKeycloak.Config config) {
		((TextInputEditText)findViewById(R.id.authserver)).setText(config.authServerUrl);
		((TextInputEditText)findViewById(R.id.realm)).setText(config.realm);
		((TextInputEditText)findViewById(R.id.clientid)).setText(config.client);
	}

	/**
	 * @return A Keycloak config object created from merging the default config
	 * with the current values displayed in the UI.
	 */
	private RxKeycloak.Config extractConfigFromUI(final RxKeycloak.Config defaultConfig) {
		return new RxKeycloak.Config(
				((TextInputEditText)findViewById(R.id.authserver)).getText().toString(),
				((TextInputEditText)findViewById(R.id.realm)).getText().toString(),
				((TextInputEditText)findViewById(R.id.clientid)).getText().toString(),
				defaultConfig.redirectUri
		);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Apply View:
		setContentView(R.layout.activity_login);

		//Populate the UI from cached/default config data:
		mDestroyDisposeBag.add(
			Single.just(getDefaultConfig())
			.subscribe(config -> applyConfigToUI(config))
		);

		mDestroyDisposeBag.add(
			RxView
				//Capture Login Button Click:
				.clicks(findViewById(R.id.loginButton))

				//Clear Results
				.doOnNext(v -> ((TextView)findViewById(R.id.keycloakResponse)).setText(""))

				//Start Auth:
				.flatMapMaybe(v ->
					RxKeycloak.service(this)
					.flatMapMaybe(authService ->

		                //Build config object for Keycloak based on form field values:
					    Single.just(extractConfigFromUI(getDefaultConfig()))

					    //Make auth request to keycloak server:
					    .flatMap(config -> RxKeycloak.auth(this, authService, config, AUTH_REQUEST_CODE))

					    //Wait for auth result on Activity:
					    .flatMapMaybe(config -> RxKeycloak.authResult(mOnActivityResult, AUTH_REQUEST_CODE))

					    //Exchange code for accessToken
					    .flatMapSingle(response -> RxKeycloak.exchangeTokens(authService, response))

					).firstOrError().onErrorComplete()
				)

				//Populate result with the parsed token body:
				.doOnNext(r -> {
					final RxKeycloak.JWT jwt = new RxKeycloak.JWT(r.accessToken);
					((TextView)findViewById(R.id.keycloakResponse)).setText(jwt.body);
				})
				.subscribe()
		);
	}

	@Override
	protected void onDestroy() {
		//Dispose of all streams:
		mDestroyDisposeBag.dispose();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mOnActivityResult.onNext(new RxActivity.ActivityResult(requestCode, resultCode, data));
	}
}
