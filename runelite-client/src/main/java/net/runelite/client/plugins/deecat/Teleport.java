package net.runelite.client.plugins.deecat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public
enum Teleport
{
    AMULET_OF_GLORY(1704, "1"),
    AMULET_OF_GLORY1(1706, "1"),
    AMULET_OF_GLORY2(1708, "1"),
    AMULET_OF_GLORY3(1710, "1"),
    AMULET_OF_GLORY4(1712, "1"),
    AMULET_OF_GLORY5(1712, "1"),
    AMULET_OF_GLORY6(1712, "1");

    private final int itemID;
    private final String key;
}
