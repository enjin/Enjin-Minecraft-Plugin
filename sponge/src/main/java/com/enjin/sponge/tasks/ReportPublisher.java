package com.enjin.sponge.tasks;

import com.enjin.common.utils.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportPublisher implements Runnable {
    final CommandSource sender;
    final StringBuilder builder;

    public ReportPublisher(CommandSource sender, StringBuilder builder) {
        this.sender = sender;
        this.builder = builder;
    }

    @Override
    public void run() {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        builder.append('\n')
                .append("=========================================")
                .append('\n')
                .append("Enjin HTTPS Test: ")
                .append(ConnectionUtil.testHTTPSconnection() ? "PASSED" : "FAILED")
                .append('\n')
                .append("Enjin HTTP Test: ")
                .append(ConnectionUtil.testHTTPconnection() ? "PASSED" : "FAILED")
                .append('\n')
                .append("Web Connectivity Test: ")
                .append(ConnectionUtil.testWebConnection() ? "PASSED" : "FAILED")
                .append('\n')
                .append("=========================================")
                .append('\n');
        String report = builder.toString().replace("authkey=\\w{50}", "authkey=HIDDEN");

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        File outFile = new File(plugin.getConfigDir(),
                "reports/" + format.format(date) + ".txt");
        BufferedWriter out = null;
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.mkdirs();
            }

            out = new BufferedWriter(new FileWriter(outFile));
            out.write(report);
            sender.sendMessage(Text.of(TextColors.GOLD, "Enjin debug report created in " + outFile.getPath()));
        } catch (IOException e) {
            sender.sendMessage(Text.of(TextColors.RED, "Unable to write Enjin debug report."));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Enjin.getLogger().log(e);
                }
            }
        }
    }
}
