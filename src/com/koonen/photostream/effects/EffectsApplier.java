package com.koonen.photostream.effects;

import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.koonen.photostream.GridLayout;
import com.koonen.photostream.settings.UserPreferences;

public class EffectsApplier {

	private GridLayout mGrid;
	private TypeEffect typeEffect;

	public EffectsApplier(GridLayout grid, UserPreferences userPreferences) {
		mGrid = grid;
		typeEffect = userPreferences.getTypeEffect();
		if (typeEffect == TypeEffect.RANDOM_EFFECT) {
			typeEffect = TypeEffect.GenerateTypeEffect();
		}
	}

	private Animation createAnimationForChild(int childIndex) {

		Animation animation = null;
		if (typeEffect == TypeEffect.ALPHA_EFFECT) {
			animation = EffectsFactory.createAlphaAnimation();
		} else if (typeEffect == TypeEffect.TRANSLATE_EFFECT) {
			boolean firstColumn = (childIndex & 0x1) == 0;
			animation = EffectsFactory.createTranslateAnimation(firstColumn);
		} else if (typeEffect == TypeEffect._3D_ROTATE_EFFECT) {
			// Create a new 3D rotation with the supplied parameter
			animation = EffectsFactory.create3DAnimation(
					mGrid.getWidth() / 6.0f, mGrid.getHeight() / 6.0f);
		} else if (typeEffect == TypeEffect.SCALE_EFFECT) {
			// 0.5f, 1.5f, 0.5f, 1.5f, 0.5f, 0.5f, 200
			animation = EffectsFactory.createScaleAnimation();
		} else {
			// TODO: Generate exception
		}
		return animation;
	}

	public void applyEffects(ImageView imageView, Bitmap bitmap) {
		imageView.setImageBitmap(bitmap);
		Animation animation = createAnimationForChild(mGrid.getChildCount());
		if (animation != null) {
			imageView.startAnimation(animation);
		}
		mGrid.addView(imageView);
	}
}
