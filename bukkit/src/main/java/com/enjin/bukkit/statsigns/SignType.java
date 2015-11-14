package com.enjin.bukkit.statsigns;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SignType {
    DONATION(null, SubType.ITEMID),
    TOPVOTER(SubType.MONTH, SubType.DAY, SubType.WEEK, SubType.MONTH),
    VOTER(null),
    TOPPLAYER(null),
    TOPPOSTER(null),
    TOPLIKES(null),
    NEWMEMBER(null),
    TOPPOINTS(null),
    POINTSSPENT(SubType.TOTAL, SubType.DAY, SubType.WEEK, SubType.MONTH, SubType.TOTAL),
    MONEYSPENT(SubType.TOTAL, SubType.DAY, SubType.WEEK, SubType.MONTH, SubType.TOTAL);

    private Pattern pattern;
    @Getter
    private SubType defaultSubType;
    @Getter
    private List<SubType> supportedSubTypes = Lists.newArrayList();

    SignType(SubType defaultSubType, SubType... supportedSubTypes) {
        this.pattern = Pattern.compile("\\[" + name().toLowerCase() + "([1-9]|10)\\]");
        this.defaultSubType = defaultSubType;
        this.supportedSubTypes.addAll(Arrays.asList(supportedSubTypes));
    }

    public Optional<Integer> matches(String line) {
        Matcher matcher = pattern.matcher(line.toLowerCase());

        if (matcher.matches()) {
            try {
                return Optional.ofNullable(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public enum SubType {
        DAY,
        WEEK,
        MONTH,
        TOTAL,
        ITEMID
    }
}
