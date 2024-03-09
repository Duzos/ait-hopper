package mc.duzo.addons.hopper.item;

import mdteam.ait.api.tardis.LinkableItem;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.control.impl.SecurityControl;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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

			TardisUtil.teleportInside(tardis, user);

			stack.damage(1, user.getWorld().getRandom(), serverUser);
			serverUser.getItemCooldownManager().set(this, 16 * 20);
		}

		return (tardis == null) ? TypedActionResult.fail(stack) : TypedActionResult.success(stack, true);
	}

	private static boolean isSecurityEnabled(Tardis tardis) {
		return PropertiesHandler.getBool(tardis.getHandlers().getProperties(), "security");
	}
	private static boolean hasMatchingKey(ServerPlayerEntity player, Tardis tardis) {
		return SecurityControl.hasMatchingKey(player, tardis);
	}
}
