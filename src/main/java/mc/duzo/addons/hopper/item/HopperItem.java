package mc.duzo.addons.hopper.item;

import mdteam.ait.api.tardis.LinkableItem;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.control.impl.SecurityControl;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HopperItem extends LinkableItem {
	public HopperItem(Settings settings) {
		super(settings.maxCount(1).maxDamageIfAbsent(16));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		Tardis tardis = getTardis(stack);

		if (!world.isClient()) {
			ServerPlayerEntity serverUser = (ServerPlayerEntity) user;

			if (isSecurityEnabled(tardis)) {
				if (!hasMatchingKey(serverUser, tardis)) {
					return TypedActionResult.fail(stack);
				}
			}

			TardisTravel.State state = tardis.getTravel().getState();

			if (state != TardisTravel.State.LANDED) return TypedActionResult.fail(stack);

			createTeleportEffect(serverUser);
			TardisUtil.teleportInside(tardis, user);

			stack.damage(1, user.getWorld().getRandom(), serverUser);
			serverUser.getItemCooldownManager().set(this, 16 * 20);

			createTeleportEffect(serverUser); // fixme doesnt seem to work.
		}

		return (tardis == null) ? TypedActionResult.fail(stack) : TypedActionResult.success(stack, true);
	}

	private static boolean isSecurityEnabled(Tardis tardis) {
		return PropertiesHandler.getBool(tardis.getHandlers().getProperties(), "security");
	}
	private static boolean hasMatchingKey(ServerPlayerEntity player, Tardis tardis) {
		return SecurityControl.hasMatchingKey(player, tardis);
	}

	/**
	 * Creates a spiral of particles around the player
	 * <br>
	 * from <a href="https://github.com/Duzos/vortex-manipulator/blob/trunk/src/main/java/mc/duzo/vortex/util/VortexUtil.java">this mod</a>
	 */
	private static void createTeleportEffect(ServerWorld world, Vec3d source) {
		double b = Math.PI / 8;

		Vec3d pos;
		double x;
		double y;
		double z;

		for(double t = 0.0D; t <= Math.PI * 2; t += Math.PI / 16) {
			for (int i = 0; i <= 1; i++) {
				x = 0.4D * (Math.PI * 2 - t) * 0.5D * Math.cos(t + b + i * Math.PI);
				y = 0.5D * t;
				z = 0.4D * (Math.PI * 2 - t) * 0.5D * Math.sin(t + b + i * Math.PI);
				pos = source.add(x, y, z);

				world.spawnParticles(ParticleTypes.FIREWORK, pos.getX(), pos.getY(), pos.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}
	private static void createTeleportEffect(ServerPlayerEntity player) {
		Vec3d dest = player.getBlockPos().toCenterPos().subtract(0, 0.5, 0);
		createTeleportEffect(player.getServerWorld(), dest);
	}
}
