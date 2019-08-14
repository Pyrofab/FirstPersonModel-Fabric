package de.tr7zw.firstperson.mixins;

import de.tr7zw.firstperson.FirstPersonModelMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntityRenderer.class, priority = 1500)
public abstract class PlayerRenderMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>{

	/**
	 * Just needed because of the extends
	 *
	 * @param entityRenderDispatcher_1
	 * @param entityModel_1
	 * @param float_1
	 */
	public PlayerRenderMixin(EntityRenderDispatcher entityRenderDispatcher_1,
			PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
		super(entityRenderDispatcher_1, entityModel_1, float_1);
	}

	@Unique private final float sneakBodyOffset = 0.27f;
	@Unique private final float swimUpBodyOffset = 0.60f;
	@Unique private final float swimDownBodyOffset = 0.50f;
	@Unique private final float inVehicleBodyOffset = 0.20f;

	@Inject(at = @At("RETURN"), method = "setModelPose")
	private void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity_1, CallbackInfo info) {
		if(!FirstPersonModelMod.renderingFirstPersonPlayer) return;

		PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = this.getModel();
		playerEntityModel_1.head.visible = false;
		playerEntityModel_1.headwear.visible = false;
	}

	@Unique private AbstractClientPlayerEntity abstractClientPlayerEntity_1;
	@Unique private double realYaw;

	@ModifyVariable(method = "method_4215",
			at = @At("HEAD"),
			ordinal = 0
			)
	public AbstractClientPlayerEntity playerGetter(AbstractClientPlayerEntity abstractClientPlayerEntity_1) {
		if(abstractClientPlayerEntity_1 == MinecraftClient.getInstance().player && MinecraftClient.getInstance().options.perspective == 0) {
			this.abstractClientPlayerEntity_1 = abstractClientPlayerEntity_1;
			realYaw = MathHelper.lerpAngleDegrees(MinecraftClient.getInstance().getTickDelta(), abstractClientPlayerEntity_1.prevYaw, abstractClientPlayerEntity_1.yaw);
		}else {
			this.abstractClientPlayerEntity_1 = null;
		}
		return abstractClientPlayerEntity_1;
	}


	@ModifyVariable(method = "method_4215",
			at = @At("HEAD"),
			ordinal = 0,
			index = 1
			)
	public double playerCordX(double x) {
		if (abstractClientPlayerEntity_1 != null && FirstPersonModelMod.renderingFirstPersonPlayer) {

			float bodyOffset;
			if(abstractClientPlayerEntity_1.isSneaking()){
				bodyOffset = sneakBodyOffset;
			}else if(MinecraftClient.getInstance().player.isInSwimmingPose()) {
				if(abstractClientPlayerEntity_1.prevPitch > 0) {
					bodyOffset = swimUpBodyOffset;
				}else {
					bodyOffset = swimDownBodyOffset;
				}
			}else if(abstractClientPlayerEntity_1.hasVehicle()) {
				bodyOffset = inVehicleBodyOffset;
			}else{
				bodyOffset = 0.25f;
			}
			x += bodyOffset * Math.sin(Math.toRadians(realYaw));
		}

		return x;
	}

	@ModifyVariable(method = "method_4215",
			at = @At("HEAD"),
			ordinal = 1,
			index = 2
			)
	public double playerCordY(double y) {
		if (abstractClientPlayerEntity_1 != null && FirstPersonModelMod.renderingFirstPersonPlayer) {

			if(MinecraftClient.getInstance().player.isInSwimmingPose()) {
				if(abstractClientPlayerEntity_1.prevPitch > 0  && abstractClientPlayerEntity_1.isInWater()) {
					y += 0.6f * Math.sin(Math.toRadians(abstractClientPlayerEntity_1.prevPitch));
				}else {
					y += 0.01f * -Math.sin(Math.toRadians(abstractClientPlayerEntity_1.prevPitch));
				}
			}
		}

		return y;
	}

	@ModifyVariable(method = "method_4215",
			at = @At("HEAD"),
			ordinal = 2,
			index = 3
			)
	public double playerCordZ(double z) {
		if (abstractClientPlayerEntity_1 != null && FirstPersonModelMod.renderingFirstPersonPlayer) {

			float bodyOffset;
			if(abstractClientPlayerEntity_1.isSneaking()){
				bodyOffset = sneakBodyOffset;
			}else if(MinecraftClient.getInstance().player.isInSwimmingPose()) {
				if(abstractClientPlayerEntity_1.prevPitch > 0) {
					bodyOffset = swimUpBodyOffset;
				}else {
					bodyOffset = swimDownBodyOffset;
				}
			}else if(abstractClientPlayerEntity_1.hasVehicle()) {
				bodyOffset = inVehicleBodyOffset;
			}else{
				bodyOffset = 0.25f;
			}
			z -= bodyOffset * Math.cos(Math.toRadians(realYaw));
		}

		return z;
	}

	@Inject(method = {"renderRightArm", "renderLeftArm"}, at = @At("HEAD"), cancellable = true)
	private void cancelArmsRender(AbstractClientPlayerEntity player, CallbackInfo ci) {
		ci.cancel();
	}

}
