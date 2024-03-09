package mc.duzo.addons.hopper;

import io.wispforest.owo.itemgroup.OwoItemSettings;
import mc.duzo.addons.hopper.item.HopperItem;
import mdteam.ait.AITMod;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HopperMod implements ModInitializer {
    public static final String MOD_ID = "hopper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "hopper_item"), new HopperItem(new OwoItemSettings().group(AITMod.AIT_ITEM_GROUP)));
    }
}
