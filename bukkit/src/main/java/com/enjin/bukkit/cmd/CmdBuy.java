package com.enjin.bukkit.cmd;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.modules.impl.PurchaseModule;
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
import java.util.function.Consumer;

import static com.enjin.bukkit.enums.Permission.CMD_BUY;

public class CmdBuy extends EnjinCommand {

    public CmdBuy(EnjinCommand parent) {
        super(parent.plugin, parent);
        String command = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
        if (command != null && !command.equals("buy"))
            this.aliases.add(command);
        else
            this.aliases.add("buy");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(CMD_BUY)
                .requireValidKey()
                .build();
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (!config.isUseBuyGUI()) {
            addSubCommand(new CmdBuyPage(this));
            addSubCommand(new CmdBuyItem(this));
        }
        addSubCommand(new CmdBuyConfirm(this));
        addSubCommand(new CmdBuyShop(this));
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
            new FetchShop(context, this::execute).runTaskAsynchronously(plugin);
            return;
        }

        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (!config.isUseBuyGUI()) {
            showText(context, instance);
            return;
        }

        Map<UUID, Menu> guiInstances = ShopListener.getGuiInstances();
        Menu menu = guiInstances.get(player.getUniqueId());
        if (menu == null)
            menu = new ShopList(player);
        menu.openMenu(player);
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

    private int getArgAsInt(CommandContext context, int index, int defaultVal) {
        Optional<Integer> optional = context.argToInt(index);
        int val = defaultVal;

        if (!optional.isPresent()) {
            Optional<Integer> optionalId = context.argToInt(index);
            if (!optionalId.isPresent())
                Translation.Command_Buy_InvalidIdFormat.send(context);
            else
                val = Math.max(optionalId.get(), 1);
        }

        return val;
    }

    class FetchShop extends BukkitRunnable {
        private CommandContext context;
        private Consumer<CommandContext> consumer;

        public FetchShop(CommandContext context, Consumer<CommandContext> consumer) {
            this.context = context;
            this.consumer = consumer;
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

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> consumer.accept(context));
        }
    }

    class CmdBuyPage extends EnjinCommand {
        public CmdBuyPage(EnjinCommand parent) {
            super(parent);
            this.aliases.add("page");
            this.aliases.add("pg");
            this.aliases.add("p");
            this.requirements = CommandRequirements.builder(parent.plugin)
                    .withAllowedSenderTypes(SenderType.PLAYER)
                    .withPermission(CMD_BUY)
                    .requireValidKey()
                    .build();
        }

        @Override
        public void execute(CommandContext context) {
            Player player = context.player;
            if (!player.isOnline())
                return;

            Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
            PlayerShopInstance instance = instances.get(player.getUniqueId());
            if (shouldFetchShops(instance)) {
                new FetchShop(context, this::execute).runTaskAsynchronously(plugin);
                return;
            }

            if (instance.getActiveShop() == null) {
                Translation.Command_Buy_NoActiveShop.send(context);
                return;
            }

            TextShopUtil.sendTextShop(player, instance, getArgAsInt(context, 0, -1));
        }

        @Override
        public Translation getUsageTranslation() {
            return Translation.Command_Buy_Page_Description;
        }
    }

    class CmdBuyItem extends EnjinCommand {

        public CmdBuyItem(EnjinCommand parent) {
            super(parent);
            this.aliases.add("item");
            this.aliases.add("it");
            this.aliases.add("i");
            this.requirements = CommandRequirements.builder(parent.plugin)
                    .withAllowedSenderTypes(SenderType.PLAYER)
                    .withPermission(CMD_BUY)
                    .requireValidKey()
                    .build();
        }

        @Override
        public void execute(CommandContext context) {
            Player player = context.player;
            if (!player.isOnline())
                return;

            Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
            PlayerShopInstance instance = instances.get(player.getUniqueId());
            if (shouldFetchShops(instance)) {
                new FetchShop(context, this::execute).runTaskAsynchronously(plugin);
                return;
            }

            Shop shop = instance.getActiveShop();
            if (shop == null) {
                Translation.Command_Buy_NoActiveShop.send(context);
                return;
            }

            Category category = instance.getActiveCategory();
            if (category == null) {
                Translation.Command_Buy_NoActiveCategory.send(context);
                return;
            }

            List<Item> items = category.getItems();
            if (items.isEmpty()) {
                Translation.Command_Buy_NoItemsDetected.send(context);
                return;
            }

            int id = Math.max(getArgAsInt(context, 0, -1), 1);
            Item item = category.getItems().get(id - 1);
            PurchaseModule module = plugin.getModuleManager().getModule(PurchaseModule.class);

            module.processItemPurchase(player, instance.getActiveShop(), item);
        }

        @Override
        public Translation getUsageTranslation() {
            return Translation.Command_Buy_Item_Description;
        }
    }

    class CmdBuyConfirm extends EnjinCommand {

        public CmdBuyConfirm(EnjinCommand parent) {
            super(parent);
            this.aliases.add("confirm");
            this.aliases.add("conf");
            this.aliases.add("cf");
            this.requirements = CommandRequirements.builder(parent.plugin)
                    .withAllowedSenderTypes(SenderType.PLAYER)
                    .withPermission(CMD_BUY)
                    .requireValidKey()
                    .build();
        }

        @Override
        public void execute(CommandContext context) {
            Player player = context.player;
            if (!player.isOnline())
                return;

            PurchaseModule module = plugin.getModuleManager().getModule(PurchaseModule.class);

            if (!module.purchasePending(player)) {
                Translation.Command_Buy_Confirm_NotPending.send(context);
                return;
            }

            module.confirmPurchase(player);
        }

        @Override
        public Translation getUsageTranslation() {
            return Translation.Command_Buy_Confirm_Description;
        }
    }

    class CmdBuyShop extends EnjinCommand {
        public CmdBuyShop(EnjinCommand parent) {
            super(parent);
            this.aliases.add("shop");
            this.aliases.add("sh");
            this.aliases.add("s");
            this.requirements = CommandRequirements.builder(parent.plugin)
                    .withAllowedSenderTypes(SenderType.PLAYER)
                    .withPermission(CMD_BUY)
                    .requireValidKey()
                    .build();
        }

        @Override
        public void execute(CommandContext context) {
            Player player = context.player;
            if (!player.isOnline())
                return;

            Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
            PlayerShopInstance instance = instances.get(player.getUniqueId());
            if (shouldFetchShops(instance)) {
                new FetchShop(context, this::execute).runTaskAsynchronously(plugin);
                return;
            }

            EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
            if (config.isUseBuyGUI()) {
                Menu menu = new ShopList(player);
                menu.openMenu(player);
                return;
            }

            TextShopUtil.sendTextShop(player, instance, getArgAsInt(context, 0, -1));
        }

        @Override
        public Translation getUsageTranslation() {
            return Translation.Command_Buy_Page_Description;
        }
    }
}
