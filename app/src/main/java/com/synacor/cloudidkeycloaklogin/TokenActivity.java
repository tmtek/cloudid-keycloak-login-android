package com.synacor.cloudidkeycloaklogin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import com.jakewharton.rxbinding4.view.RxView;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class TokenActivity extends AppCompatActivity {

	private static final String DEBUG_TAG = "TokenActivity";

	private static void log(final String message) {
		Log.d(DEBUG_TAG, message);
	}

	private final CompositeDisposable mDestroyDisposeBag = new CompositeDisposable();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_token);

		mDestroyDisposeBag.add(
			RxView
				.clicks(findViewById(R.id.logoutButton))
				.flatMapSingle(v -> RXUtil.loginActivity(TokenActivity.this))
				.subscribe()
		);
	}

	@Override
	protected void onDestroy() {
		mDestroyDisposeBag.dispose();
		super.onDestroy();
	}
}
