package com.synacor.cloudidkeycloaklogin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import com.jakewharton.rxbinding4.view.RxView;

public class LoginActivity extends AppCompatActivity {

	private static final String DEBUG_TAG = "MainActivity";

	private static void log(final String message) {
		Log.d(DEBUG_TAG, message);
	}

	private final CompositeDisposable mDestroyDisposeBag = new CompositeDisposable();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mDestroyDisposeBag.add(
				RxView
				.clicks(findViewById(R.id.loginButton))
				.flatMapSingle(v -> RXUtil.tokenActivity(LoginActivity.this))
				.subscribe()
		);
	}

	@Override
	protected void onDestroy() {
		mDestroyDisposeBag.dispose();
		super.onDestroy();
	}
}
