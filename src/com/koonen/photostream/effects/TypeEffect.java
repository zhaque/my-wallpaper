package com.koonen.photostream.effects;

import java.util.Collection;
import java.util.Random;

import com.koonen.utils.Enumeration;

public class TypeEffect extends Enumeration {

	// public static final TypeEffect UNKNOWN_EFFECT = new TypeEffect("Unknown",
	// "unknown");
	public static final TypeEffect RANDOM_EFFECT = new TypeEffect("random",
			"random");
	public static final TypeEffect TRANSLATE_EFFECT = new TypeEffect(
			"translate", "translate");
	public static final TypeEffect ALPHA_EFFECT = new TypeEffect("Alpha",
			"alpha");
	public static final TypeEffect _3D_ROTATE_EFFECT = new TypeEffect(
			"3dRotate", "3dRotate");
	public static final TypeEffect SCALE_EFFECT = new TypeEffect("scale",
			"scale");

	static {
		// add(TypeEffect.class, UNKNOWN_EFFECT);
		add(TypeEffect.class, RANDOM_EFFECT);
		add(TypeEffect.class, TRANSLATE_EFFECT);
		add(TypeEffect.class, ALPHA_EFFECT);
		add(TypeEffect.class, _3D_ROTATE_EFFECT);
		add(TypeEffect.class, SCALE_EFFECT);
	}

	
	public static TypeEffect GenerateTypeEffect() {
		Collection<Enumeration> values = RANDOM_EFFECT.values(TypeEffect.class);
		int num = Math.abs(new Random().nextInt()) % values.size();
		TypeEffect typeEffect = (TypeEffect) values.toArray()[num];
		if (typeEffect == RANDOM_EFFECT) {
			typeEffect = _3D_ROTATE_EFFECT;
		}
		return typeEffect;
		
	}

	private TypeEffect(String name, String value) {
		super(name, value);
	}

	public static TypeEffect valueOf(String name) {
		TypeEffect result = null;
		result = (TypeEffect) valueOf(TypeEffect.class, name);
		return result;
	}
}
