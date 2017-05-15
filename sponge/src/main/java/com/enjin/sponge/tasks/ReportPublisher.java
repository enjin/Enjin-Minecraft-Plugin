package com.enjin.sponge.tasks;

import com.enjin.rpc.util.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
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
        try (InputStream in = new ByteArrayInputStream(report.getBytes())) {
            ZipFile zip = new ZipFile(new File("enjinreport_" + format.format(date) + ".zip"));
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            zip.addFile(Enjin.getLogger().getLogFile(), parameters);
            parameters.setFileNameInZip("enjinreport_" + format.format(date) + ".txt");
            parameters.setSourceExternalStream(true);
            zip.addStream(in, parameters);
            sender.sendMessage(Text.builder()
                    .color(TextColors.GOLD)
                    .append(Text.of("Enjin report created in " + zip.getFile().getPath() + " successfully!"))
                    .build());
        } catch (Exception e) {
            sender.sendMessage(Text.builder()
                    .color(TextColors.DARK_RED)
                    .append(Text.of("Unable to write Enjin report!"))
                    .build());
            Enjin.getLogger().log(e);
        }
    }

}
