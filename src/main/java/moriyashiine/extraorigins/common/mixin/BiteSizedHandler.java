package moriyashiine.extraorigins.common.mixin;

import moriyashiine.extraorigins.common.registry.EOPowers;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class BiteSizedHandler extends LivingEntity {
	@Shadow
	@Final
	private static Map<EntityPose, EntityDimensions> POSE_DIMENSIONS;
	
	protected BiteSizedHandler(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Inject(method = "tick", at = @At("TAIL"), cancellable = true)
	private void tick(CallbackInfo callbackInfo)
	{
		calculateDimensions();
	}
	
	@Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
	private void getDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> callbackInfo)
	{
		if (EOPowers.BITE_SIZED.isActive(this))
		{
			callbackInfo.setReturnValue(POSE_DIMENSIONS.getOrDefault(pose, PlayerEntity.STANDING_DIMENSIONS).scaled(0.25f));
		}
	}
	
	@Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
	private void getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> callbackInfo)
	{
		if (age > 0 && EOPowers.BITE_SIZED.isActive(this))
		{
			callbackInfo.setReturnValue(super.getActiveEyeHeight(pose, dimensions) * 0.5f);
		}
	}
	
	@Mixin(LivingEntity.class)
	private static class Baby
	{
		@Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
		private void isBaby(CallbackInfoReturnable<Boolean> callbackInfo)
		{
			Object obj = this;
			//noinspection ConstantConditions
			if (obj instanceof PlayerEntity && EOPowers.BITE_SIZED.isActive((Entity) obj))
			{
				callbackInfo.setReturnValue(true);
			}
		}
	}
	
	@Mixin(Entity.class)
	private static class JumpVelocity
	{
		@Inject(method = "getJumpVelocityMultiplier", at = @At("HEAD"), cancellable = true)
		private void getJumpVelocityMultiplier(CallbackInfoReturnable<Float> callbackInfo)
		{
			Object obj = this;
			//noinspection ConstantConditions
			if (EOPowers.BITE_SIZED.isActive((Entity) obj))
			{
				callbackInfo.setReturnValue(0.5f);
			}
		}
	}
	
	@Mixin(TrackTargetGoal.class)
	private static abstract class Tracker extends Goal
	{
		@Shadow
		@Final
		protected MobEntity mob;
		
		@Shadow
		protected LivingEntity target;
		
		@Shadow
		protected abstract double getFollowRange();
		
		@Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
		private void shouldContinue(CallbackInfoReturnable<Boolean> callbackInfo)
		{
			if (target != null && EOPowers.BITE_SIZED.isActive(target))
			{
				double range = getFollowRange() / 2;
				if (mob.squaredDistanceTo(target) > range * range)
				{
					stop();
					callbackInfo.setReturnValue(false);
				}
			}
		}
	}
}