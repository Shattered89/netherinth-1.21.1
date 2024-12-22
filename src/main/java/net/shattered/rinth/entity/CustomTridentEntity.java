package net.shattered.rinth.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shattered.rinth.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class CustomTridentEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private boolean dealtDamage;
    public int returnTimer;

    public CustomTridentEntity(EntityType<? extends CustomTridentEntity> entityType, World world) {
        super(entityType, world);
        ItemStack defaultStack = new ItemStack(ModItems.PITCHFORK);
        this.setStack(defaultStack);
    }

    public CustomTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.CUSTOM_TRIDENT, owner, world, stack, null);
        if (stack != null) {
            this.setStack(stack.copy());
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
            this.dataTracker.set(ENCHANTED, stack.hasGlint());
        }
    }

    public CustomTridentEntity(World world, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.CUSTOM_TRIDENT, x, y, z, world, stack, stack);
        if (stack != null) {
            this.setStack(stack.copy());
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
            this.dataTracker.set(ENCHANTED, stack.hasGlint());
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LOYALTY, (byte)0);
        builder.add(ENCHANTED, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        int i = this.dataTracker.get(LOYALTY);
        if (i > 0 && (this.dealtDamage || this.isNoClip()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d vec3d = entity.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * (double)i, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }

                double d = 0.05 * (double)i;
                this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.returnTimer++;
            }
        }

        super.tick();
    }

    private boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        return entity != null && entity.isAlive() && (!(entity instanceof ServerPlayerEntity) || !entity.isSpectator());
    }

    public boolean isEnchanted() {
        return this.dataTracker.get(ENCHANTED);
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModItems.PITCHFORK);
    }

    private byte getLoyalty(ItemStack stack) {
        return this.getWorld() instanceof ServerWorld serverWorld ?
                (byte)MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(serverWorld, stack, this), 0, 127) : 0;
    }

    @Override
    protected float getDragInWater() {
        return 0.99F;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float damage = 18.0F;
        Entity owner = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, (Entity)(owner == null ? this : owner));

        ItemStack weaponStack = this.getWeaponStack();
        if (weaponStack != null && this.getWorld() instanceof ServerWorld serverWorld) {
            damage = EnchantmentHelper.getDamage(serverWorld, weaponStack, entity, damageSource, damage);
        }

        this.dealtDamage = true;
        if (entity.damage(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                if (weaponStack != null && this.getWorld() instanceof ServerWorld serverWorld) {
                    EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, weaponStack);
                }
                this.knockback(livingEntity, damageSource);
                this.onHit(livingEntity);
            }
        }

        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Nullable
    @Override
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dealtDamage = nbt.getBoolean("DealtDamage");
        ItemStack stack = this.getItemStack();
        if (stack != null) {
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("DealtDamage", this.dealtDamage);
    }
}