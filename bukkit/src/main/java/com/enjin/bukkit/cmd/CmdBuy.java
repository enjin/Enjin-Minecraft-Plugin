package com.enjin.bukkit.cmd;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.shop.TextShopUtil;
import com.enjin.bukkit.shop.gui.ShopList;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCError;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.enjin.bukkit.enums.Permission.CMD_BUY;

public class CmdBuy extends EnjinCommand {

    public CmdBuy(EnjinCommand parent) {
        super(parent.plugin, parent);
        String command = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
        if (command != null)
            this.aliases.add(command);
        this.aliases.add("buy");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(CMD_BUY)
                .requireValidKey()
                .build();
        register(this.aliases.get(0), new ArrayList<>(0));
    }

    @Override
    public void execute(CommandContext context) {
        Player player = context.player;
        if (!player.isOnline())
            return;

        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        PlayerShopInstance instance = instances.get(player.getUniqueId());
        if (shouldFetchShops(instance)) {
            new FetchShop(context).runTaskAsynchronously(plugin);
            return;
        }

        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (config.isUseBuyGUI()) {
            Map<UUID, Menu> guiInstances = ShopListener.getGuiInstances();
            Menu menu = guiInstances.get(player.getUniqueId());
            if (menu == null)
                menu = new ShopList(player);
            menu.openMenu(player);
            return;
        }

        showText(context, instance);
    }

    private void showText(CommandContext context, PlayerShopInstance instance) {
        Shop activeShop = instance.getActiveShop();
        Category activeCategory = instance.getActiveCategory();

        if (context.args.isEmpty()) {
            if (activeCategory != null)
                instance.updateCategory(-1);
        } else {
            Optional<Integer> optionalId = context.argToInt(0);
            if (!optionalId.isPresent()) {
                Translation.Command_Buy_InvalidIdFormat.send(context);
                return;
            }

            int id = Math.max(optionalId.get(), 1);

            if (activeCategory != null) {
                List<Category> categories = activeCategory.getCategories();
                if (categories == null || categories.isEmpty()) {
                    sendTextItem(instance, context.player, id);
                    return;
                }
            }

            if (activeShop == null)
                updateShop(instance, id);
            else
                updateCategory(instance, id);
        }

        TextShopUtil.sendTextShop(context.player, instance, -1);
    }

    private void sendTextItem(PlayerShopInstance instance, Player player, int id) {
        Category category = instance.getActiveCategory();
        List<Item> items = category.getItems();
        id = Math.min(id, items.size());
        if (items.isEmpty()) {
            Translation.Command_Buy_NoItemsDetected.send(player);
            return;
        } else {
            TextShopUtil.sendItemInfo(player, instance, id - 1);
        }
    }

    private void updateShop(PlayerShopInstance instance, int id) {
        List<Shop> shops = instance.getShops();
        if (shops.size() < id)
            instance.updateShop(shops.size() - 1);
        else
            instance.updateShop(id - 1);
    }

    private void updateCategory(PlayerShopInstance instance, int id) {
        List<Category> categories = instance.getActiveCategory() == null
                ? instance.getActiveShop().getCategories()
                : instance.getActiveCategory().getCategories();

        if (categories.size() < id)
            instance.updateCategory(categories.size() - 1);
        else
            instance.updateCategory(id - 1);
    }

    private boolean shouldFetchShops(PlayerShopInstance instance) {
        if (instance == null)
            return true;
        return (System.currentTimeMillis() - instance.getLastUpdated()) > TimeUnit.MINUTES.toMillis(10);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Buy_Description;
    }

    class FetchShop extends BukkitRunnable {
        private CommandContext context;

        public FetchShop(CommandContext context) {
            this.context = context;
        }

        @Override
        public void run() {
            Player player = context.player;
            if (!player.isOnline())
                return;

            RPCData<List<Shop>> data = EnjinServices.getService(ShopService.class).get(player.getName());
            if (data == null) {
                Translation.Errors_Network_Connection.send(player);
                return;
            }

            RPCError error = data.getError();
            if (error != null) {
                Translation.Errors_Error.send(player, error.getMessage());
                return;
            }

            List<Shop> shops = data.getResult();
            if (shops == null || shops.isEmpty()) {
                Translation.Command_Buy_NoShopsDetected.send(player);
                return;
            }

            Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
            if (instances.containsKey(player.getUniqueId()))
                instances.get(player.getUniqueId()).update(shops);
            else
                instances.put(player.getUniqueId(), new PlayerShopInstance(shops));

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> execute(context));
        }
    }
}
