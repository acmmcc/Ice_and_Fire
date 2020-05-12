package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityDragonEgg extends LivingEntity implements IBlacklistedFromStatues, IDeadMob {

    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityDragonEgg.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> DRAGON_TYPE = EntityDataManager.createKey(EntityDragonEgg.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DRAGON_AGE = EntityDataManager.createKey(EntityDragonEgg.class, DataSerializers.VARINT);

    public EntityDragonEgg(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    public void writeEntityToNBT(CompoundNBT tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Color", (byte) this.getType().ordinal());
        tag.setByte("DragonAge", (byte) this.getDragonAge());
        if (this.getOwnerId() == null) {
            tag.setString("OwnerUUID", "");
        } else {
            tag.setString("OwnerUUID", this.getOwnerId().toString());
        }

    }

    @Override
    public void readEntityFromNBT(CompoundNBT tag) {
        super.readEntityFromNBT(tag);
        this.setType(EnumDragonEgg.values()[tag.getInteger("Color")]);
        this.setDragonAge(tag.getByte("DragonAge"));
        String s;

        if (tag.hasKey("OwnerUUID", 8)) {
            s = tag.getString("OwnerUUID");
        } else {
            String s1 = tag.getString("Owner");
            s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
        }
        if (!s.isEmpty()) {
            this.setOwnerId(UUID.fromString(s));
        }

    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(DRAGON_TYPE, Integer.valueOf(0));
        this.getDataManager().register(DRAGON_AGE, Integer.valueOf(0));
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
    }

    @Nullable
    public UUID getOwnerId() {
        return (UUID) ((Optional) this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
    }

    public void setOwnerId(@Nullable UUID p_184754_1_) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
    }

    public EnumDragonEgg getType() {
        return EnumDragonEgg.values()[this.getDataManager().get(DRAGON_TYPE).intValue()];
    }

    public void setType(EnumDragonEgg newtype) {
        this.getDataManager().set(DRAGON_TYPE, newtype.ordinal());
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource i) {
        return i.getTrueSource() != null;
    }

    public int getDragonAge() {
        return this.getDataManager().get(DRAGON_AGE).intValue();
    }

    public void setDragonAge(int i) {
        this.getDataManager().set(DRAGON_AGE, i);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.setAir(200);
        getType().dragonType.updateEggCondition(this);
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource var1, float var2) {
        if (!world.isRemote && !var1.canHarmInCreative() && !isDead) {
            this.dropItem(this.getItem().getItem(), 1);
        }
        this.setDead();
        return super.attackEntityFrom(var1, var2);
    }

    private ItemStack getItem() {
        switch (getType().ordinal()) {
            default:
                return new ItemStack(IafItemRegistry.DRAGONEGG_RED);
            case 1:
                return new ItemStack(IafItemRegistry.DRAGONEGG_GREEN);
            case 2:
                return new ItemStack(IafItemRegistry.DRAGONEGG_BRONZE);
            case 3:
                return new ItemStack(IafItemRegistry.DRAGONEGG_GRAY);
            case 4:
                return new ItemStack(IafItemRegistry.DRAGONEGG_BLUE);
            case 5:
                return new ItemStack(IafItemRegistry.DRAGONEGG_WHITE);
            case 6:
                return new ItemStack(IafItemRegistry.DRAGONEGG_SAPPHIRE);
            case 7:
                return new ItemStack(IafItemRegistry.DRAGONEGG_SILVER);

        }
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entity) {
    }

    @Override
    public boolean canBeTurnedToStone() {
        return false;
    }

    public void onPlayerPlace(PlayerEntity player) {
        this.setOwnerId(player.getUniqueID());
    }

    @Override
    public boolean isMobDead() {
        return true;
    }

    @Override
    public boolean isNoDespawnRequired() {
        return true;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }
}
