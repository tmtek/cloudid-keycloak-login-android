package com.synacor.cloudidkeycloaklogin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import io.reactivex.rxjava3.core.Single;

final  class RXUtil {

	public static Single<Boolean> startActivity(final Activity activity, final Class activityCls) {
		return Single.create(emitter -> {
			activity.startActivity(new Intent(activity, activityCls));
			activity.finish();
			emitter.onSuccess(true);
		});
	}

	public static Single<Boolean> loginActivity(final Activity activity) {
		return startActivity(activity, LoginActivity.class);
	}

	public static Single<Boolean> tokenActivity(final Activity activity) {
		return startActivity(activity, TokenActivity.class);
	}

}
