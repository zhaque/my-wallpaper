package com.koonen.photostream.effects;


import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class EffectsFactory {

	private static void setDefaultAnimationSettings(Animation animation,
			int duration) {
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setFillAfter(false);
		animation.setDuration(duration);
	}

	public static Animation createTranslateAnimation(Boolean leftToRight) {
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, leftToRight ? -1.1f : 1.1f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

		setDefaultAnimationSettings(animation, 300);

		return animation;
	}

	public static Animation create3DAnimation(float width, float height) {
		Animation animation = new Rotate3dAnimation(180, 360, width / 2.0f,
				height / 2.0f, 310.0f, false);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setInterpolator(new AccelerateInterpolator());

		return animation;
	}

	public static Animation createScaleAnimation() {
		// float fromX, float toX, float fromY, float toY, float pivotX, float
		// pivotY, int duration
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(new AccelerateInterpolator());

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(200);
		animationSet.addAnimation(animation);

		animation = new ScaleAnimation(0.5f, 1.5f, 0.5f, 1.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(200);
		animationSet.addAnimation(animation);

		animation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(200);
		animation.setStartOffset(200);
		animationSet.addAnimation(animation);

		return animationSet;
	}

	public static Animation createAlphaAnimation() {
		Animation animation = new AlphaAnimation(0.0f, 1.0f);

		setDefaultAnimationSettings(animation, 700);

		return animation;
	}
}
