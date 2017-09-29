package com.enjin.sponge.utils.text.font;

/**
 * Represents the built-in Minecraft font.
 */
public class MinecraftFont extends MapFont {

    private static final int spaceSize = 2;

    private static final String fontChars =
            " !\"#$%&'()*+,-./0123456789:;<=>?" +
                    "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_" +
                    "'abcdefghijklmnopqrstuvwxyz{|}~\u007F" +
                    "\u00C7\u00FC\u00E9\u00E2\u00E4\u00E0\u00E5\u00E7" +
                    "\u00EA\u00EB\u00E8\u00EF\u00EE\u00EC\u00C4\u00C5" +
                    "\u00C9\u00E6\u00C6\u00F4\u00F6\u00F2\u00FB\u00F9" +
                    "\u00FF\u00D6\u00DC\u00F8\u00A3\u00D8\u00D7\u0191" +
                    "\u00E1\u00ED\u00F3\u00FA\u00F1\u00D1\u00AA\u00BA" +
                    "\u00BF\u00AE\u00AC\u00BD\u00BC\u00A1\u00AB\u00BB";

    private static final int[][] fontData = new int[][]{
        /* null */  {0, 0, 0, 0, 0, 0, 0, 0},
        /* 1 */  {126, 129, 165, 129, 189, 153, 129, 126},
        /* 2 */  {126, 255, 219, 255, 195, 231, 255, 126},
        /* 3 */  {54, 127, 127, 127, 62, 28, 8, 0},
        /* 4 */  {8, 28, 62, 127, 62, 28, 8, 0},
        /* 5 */  {28, 62, 28, 127, 127, 62, 28, 62},
        /* 6 */  {8, 8, 28, 62, 127, 62, 28, 62},
        /* 7 */  {0, 0, 24, 60, 60, 24, 0, 0},
        /* 8 */  {255, 255, 231, 195, 195, 231, 255, 255},
        /* 9 */  {0, 60, 102, 66, 66, 102, 60, 0},
        /* 10 */  {255, 195, 153, 189, 189, 153, 195, 255},
        /* 11 */  {240, 224, 240, 190, 51, 51, 51, 30},
        /* 12 */  {60, 102, 102, 102, 60, 24, 126, 24},
        /* 13 */  {252, 204, 252, 12, 12, 14, 15, 7},
        /* 14 */  {254, 198, 254, 198, 198, 230, 103, 3},
        /* 15 */  {153, 90, 60, 231, 231, 60, 90, 153},
        /* 16 */  {1, 7, 31, 127, 31, 7, 1, 0},
        /* 17 */  {64, 112, 124, 127, 124, 112, 64, 0},
        /* 18 */  {24, 60, 126, 24, 24, 126, 60, 24},
        /* 19 */  {102, 102, 102, 102, 102, 0, 102, 0},
        /* 20 */  {254, 219, 219, 222, 216, 216, 216, 0},
        /* 21 */  {124, 198, 28, 54, 54, 28, 51, 30},
        /* 22 */  {0, 0, 0, 0, 126, 126, 126, 0},
        /* 23 */  {24, 60, 126, 24, 126, 60, 24, 255},
        /* 24 */  {24, 60, 126, 24, 24, 24, 24, 0},
        /* 25 */  {24, 24, 24, 24, 126, 60, 24, 0},
        /* 26 */  {0, 24, 48, 127, 48, 24, 0, 0},
        /* 27 */  {0, 12, 6, 127, 6, 12, 0, 0},
        /* 28 */  {0, 0, 3, 3, 3, 127, 0, 0},
        /* 29 */  {0, 36, 102, 255, 102, 36, 0, 0},
        /* 30 */  {0, 24, 60, 126, 255, 255, 0, 0},
        /* 31 */  {0, 255, 255, 126, 60, 24, 0, 0},
        /*   */  {0, 0, 0, 0, 0, 0, 0, 0},
        /* ! */  {1, 1, 1, 1, 1, 0, 1, 0},
        /* " */  {10, 10, 5, 0, 0, 0, 0, 0},
        /* # */  {10, 10, 31, 10, 31, 10, 10, 0},
        /* $ */  {4, 30, 1, 14, 16, 15, 4, 0},
        /* % */  {17, 9, 8, 4, 2, 18, 17, 0},
        /* & */  {4, 10, 4, 22, 13, 9, 22, 0},
        /* ' */  {2, 2, 1, 0, 0, 0, 0, 0},
        /* ( */  {12, 2, 1, 1, 1, 2, 12, 0},
        /* ) */  {3, 4, 8, 8, 8, 4, 3, 0},
        /* * */  {0, 0, 9, 6, 9, 0, 0, 0},
        /* + */  {0, 4, 4, 31, 4, 4, 0, 0},
        /* , */  {0, 0, 0, 0, 0, 1, 1, 1},
        /* - */  {0, 0, 0, 31, 0, 0, 0, 0},
        /* . */  {0, 0, 0, 0, 0, 1, 1, 0},
        /* / */  {16, 8, 8, 4, 2, 2, 1, 0},
        /* 0 */  {14, 17, 25, 21, 19, 17, 14, 0},
        /* 1 */  {4, 6, 4, 4, 4, 4, 31, 0},
        /* 2 */  {14, 17, 16, 12, 2, 17, 31, 0},
        /* 3 */  {14, 17, 16, 12, 16, 17, 14, 0},
        /* 4 */  {24, 20, 18, 17, 31, 16, 16, 0},
        /* 5 */  {31, 1, 15, 16, 16, 17, 14, 0},
        /* 6 */  {12, 2, 1, 15, 17, 17, 14, 0},
        /* 7 */  {31, 17, 16, 8, 4, 4, 4, 0},
        /* 8 */  {14, 17, 17, 14, 17, 17, 14, 0},
        /* 9 */  {14, 17, 17, 30, 16, 8, 6, 0},
        /* : */  {0, 1, 1, 0, 0, 1, 1, 0},
        /* ; */  {0, 1, 1, 0, 0, 1, 1, 1},
        /* < */  {8, 4, 2, 1, 2, 4, 8, 0},
        /* = */  {0, 0, 31, 0, 0, 31, 0, 0},
        /* > */  {1, 2, 4, 8, 4, 2, 1, 0},
        /* ? */  {14, 17, 16, 8, 4, 0, 4, 0},
        /* @ */  {30, 33, 45, 45, 61, 1, 30, 0},
        /* A */  {14, 17, 31, 17, 17, 17, 17, 0},
        /* B */  {15, 17, 15, 17, 17, 17, 15, 0},
        /* C */  {14, 17, 1, 1, 1, 17, 14, 0},
        /* D */  {15, 17, 17, 17, 17, 17, 15, 0},
        /* E */  {31, 1, 7, 1, 1, 1, 31, 0},
        /* F */  {31, 1, 7, 1, 1, 1, 1, 0},
        /* G */  {30, 1, 25, 17, 17, 17, 14, 0},
        /* H */  {17, 17, 31, 17, 17, 17, 17, 0},
        /* I */  {7, 2, 2, 2, 2, 2, 7, 0},
        /* J */  {16, 16, 16, 16, 16, 17, 14, 0},
        /* K */  {17, 9, 7, 9, 17, 17, 17, 0},
        /* L */  {1, 1, 1, 1, 1, 1, 31, 0},
        /* M */  {17, 27, 21, 17, 17, 17, 17, 0},
        /* N */  {17, 19, 21, 25, 17, 17, 17, 0},
        /* O */  {14, 17, 17, 17, 17, 17, 14, 0},
        /* P */  {15, 17, 15, 1, 1, 1, 1, 0},
        /* Q */  {14, 17, 17, 17, 17, 9, 22, 0},
        /* R */  {15, 17, 15, 17, 17, 17, 17, 0},
        /* S */  {30, 1, 14, 16, 16, 17, 14, 0},
        /* T */  {31, 4, 4, 4, 4, 4, 4, 0},
        /* U */  {17, 17, 17, 17, 17, 17, 14, 0},
        /* V */  {17, 17, 17, 17, 10, 10, 4, 0},
        /* W */  {17, 17, 17, 17, 21, 27, 17, 0},
        /* X */  {17, 10, 4, 10, 17, 17, 17, 0},
        /* Y */  {17, 10, 4, 4, 4, 4, 4, 0},
        /* Z */  {31, 16, 8, 4, 2, 1, 31, 0},
        /* [ */  {7, 1, 1, 1, 1, 1, 7, 0},
        /* \ */  {1, 2, 2, 4, 8, 8, 16, 0},
        /* ] */  {7, 4, 4, 4, 4, 4, 7, 0},
        /* ^ */  {4, 10, 17, 0, 0, 0, 0, 0},
        /* _ */  {0, 0, 0, 0, 0, 0, 0, 31},
        /* ` */  {1, 1, 2, 0, 0, 0, 0, 0},
        /* a */  {0, 0, 14, 16, 30, 17, 30, 0},
        /* b */  {1, 1, 13, 19, 17, 17, 15, 0},
        /* c */  {0, 0, 14, 17, 1, 17, 14, 0},
        /* d */  {16, 16, 22, 25, 17, 17, 30, 0},
        /* e */  {0, 0, 14, 17, 31, 1, 30, 0},
        /* f */  {12, 2, 15, 2, 2, 2, 2, 0},
        /* g */  {0, 0, 30, 17, 17, 30, 16, 15},
        /* h */  {1, 1, 13, 19, 17, 17, 17, 0},
        /* i */  {1, 0, 1, 1, 1, 1, 1, 0},
        /* j */  {16, 0, 16, 16, 16, 17, 17, 14},
        /* k */  {1, 1, 9, 5, 3, 5, 9, 0},
        /* l */  {1, 1, 1, 1, 1, 1, 2, 0},
        /* m */  {0, 0, 11, 21, 21, 17, 17, 0},
        /* n */  {0, 0, 15, 17, 17, 17, 17, 0},
        /* o */  {0, 0, 14, 17, 17, 17, 14, 0},
        /* p */  {0, 0, 13, 19, 17, 15, 1, 1},
        /* q */  {0, 0, 22, 25, 17, 30, 16, 16},
        /* r */  {0, 0, 13, 19, 1, 1, 1, 0},
        /* s */  {0, 0, 30, 1, 14, 16, 15, 0},
        /* t */  {2, 2, 7, 2, 2, 2, 4, 0},
        /* u */  {0, 0, 17, 17, 17, 17, 30, 0},
        /* v */  {0, 0, 17, 17, 17, 10, 4, 0},
        /* w */  {0, 0, 17, 17, 21, 21, 30, 0},
        /* x */  {0, 0, 17, 10, 4, 10, 17, 0},
        /* y */  {0, 0, 17, 17, 17, 30, 16, 15},
        /* z */  {0, 0, 31, 8, 4, 2, 31, 0},
        /* { */  {12, 2, 2, 1, 2, 2, 12, 0},
        /* | */  {1, 1, 1, 0, 1, 1, 1, 0},
        /* } */  {3, 4, 4, 8, 4, 4, 3, 0},
        /* ~ */  {38, 25, 0, 0, 0, 0, 0, 0},
        /* ? */  {0, 0, 4, 10, 17, 17, 31, 0},
        /* � */  {14, 17, 1, 1, 17, 14, 16, 12},
        /* � */  {10, 0, 17, 17, 17, 17, 30, 0},
        /* � */  {24, 0, 14, 17, 31, 1, 30, 0},
        /* � */  {14, 17, 14, 16, 30, 17, 30, 0},
        /* � */  {10, 0, 14, 16, 30, 17, 30, 0},
        /* � */  {3, 0, 14, 16, 30, 17, 30, 0},
        /* � */  {4, 0, 14, 16, 30, 17, 30, 0},
        /* � */  {0, 14, 17, 1, 17, 14, 16, 12},
        /* � */  {14, 17, 14, 17, 31, 1, 30, 0},
        /* � */  {10, 0, 14, 17, 31, 1, 30, 0},
        /* � */  {3, 0, 14, 17, 31, 1, 30, 0},
        /* � */  {5, 0, 2, 2, 2, 2, 2, 0},
        /* � */  {14, 17, 4, 4, 4, 4, 4, 0},
        /* � */  {3, 0, 2, 2, 2, 2, 2, 0},
        /* � */  {17, 14, 17, 31, 17, 17, 17, 0},
        /* � */  {4, 0, 14, 17, 31, 17, 17, 0},
        /* � */  {24, 0, 31, 1, 7, 1, 31, 0},
        /* � */  {0, 0, 10, 20, 30, 5, 30, 0},
        /* � */  {30, 5, 15, 5, 5, 5, 29, 0},
        /* � */  {14, 17, 14, 17, 17, 17, 14, 0},
        /* � */  {10, 0, 14, 17, 17, 17, 14, 0},
        /* � */  {3, 0, 14, 17, 17, 17, 14, 0},
        /* � */  {14, 17, 0, 17, 17, 17, 30, 0},
        /* � */  {3, 0, 17, 17, 17, 17, 30, 0},
        /* � */  {10, 0, 17, 17, 17, 30, 16, 15},
        /* � */  {17, 14, 17, 17, 17, 17, 14, 0},
        /* � */  {17, 0, 17, 17, 17, 17, 14, 0},
        /* � */  {0, 0, 14, 25, 21, 19, 14, 4},
        /* � */  {12, 18, 2, 15, 2, 2, 31, 0},
        /* � */  {14, 17, 25, 21, 19, 17, 14, 0},
        /* � */  {0, 0, 5, 2, 5, 0, 0, 0},
        /* � */  {8, 20, 4, 14, 4, 4, 5, 2},
        /* � */  {24, 0, 14, 16, 30, 17, 30, 0},
        /* � */  {3, 0, 1, 1, 1, 1, 1, 0},
        /* � */  {24, 0, 14, 17, 17, 17, 14, 0},
        /* � */  {24, 0, 17, 17, 17, 17, 30, 0},
        /* � */  {31, 0, 15, 17, 17, 17, 17, 0},
        /* � */  {31, 0, 17, 19, 21, 25, 17, 0},
        /* � */  {14, 16, 31, 30, 0, 31, 0, 0},
        /* � */  {14, 17, 17, 14, 0, 31, 0, 0},
        /* � */  {4, 0, 4, 2, 1, 17, 14, 0},
        /* � */  {0, 30, 45, 37, 43, 30, 0, 0},
        /* � */  {0, 0, 0, 31, 16, 16, 0, 0},
        /* � */  {17, 9, 8, 4, 18, 10, 25, 0},
        /* � */  {17, 9, 8, 4, 26, 26, 17, 0},
        /* � */  {0, 1, 0, 1, 1, 1, 1, 0},
        /* � */  {0, 20, 10, 5, 10, 20, 0, 0},
        /* � */  {0, 5, 10, 20, 10, 5, 0, 0},
        /* 176 */  {68, 17, 68, 17, 68, 17, 68, 17},
        /* 177 */  {170, 85, 170, 85, 170, 85, 170, 85},
        /* 178 */  {219, 238, 219, 119, 219, 238, 219, 119},
        /* 179 */  {24, 24, 24, 24, 24, 24, 24, 24},
        /* 180 */  {24, 24, 24, 24, 31, 24, 24, 24},
        /* 181 */  {24, 24, 31, 24, 31, 24, 24, 24},
        /* 182 */  {108, 108, 108, 108, 111, 108, 108, 108},
        /* 183 */  {0, 0, 0, 0, 127, 108, 108, 108},
        /* 184 */  {0, 0, 31, 24, 31, 24, 24, 24},
        /* 185 */  {108, 108, 111, 96, 111, 108, 108, 108},
        /* 186 */  {108, 108, 108, 108, 108, 108, 108, 108},
        /* 187 */  {0, 0, 127, 96, 111, 108, 108, 108},
        /* 188 */  {108, 108, 111, 96, 127, 0, 0, 0},
        /* 189 */  {108, 108, 108, 108, 127, 0, 0, 0},
        /* 190 */  {24, 24, 31, 24, 31, 0, 0, 0},
        /* 191 */  {0, 0, 0, 0, 31, 24, 24, 24},
        /* 192 */  {24, 24, 24, 24, 248, 0, 0, 0},
        /* 193 */  {24, 24, 24, 24, 255, 0, 0, 0},
        /* 194 */  {0, 0, 0, 0, 255, 24, 24, 24},
        /* 195 */  {24, 24, 24, 24, 248, 24, 24, 24},
        /* 196 */  {0, 0, 0, 0, 255, 0, 0, 0},
        /* 197 */  {24, 24, 24, 24, 255, 24, 24, 24},
        /* 198 */  {24, 24, 248, 24, 248, 24, 24, 24},
        /* 199 */  {108, 108, 108, 108, 236, 108, 108, 108},
        /* 200 */  {108, 108, 236, 12, 252, 0, 0, 0},
        /* 201 */  {0, 0, 252, 12, 236, 108, 108, 108},
        /* 202 */  {108, 108, 239, 0, 255, 0, 0, 0},
        /* 203 */  {0, 0, 255, 0, 239, 108, 108, 108},
        /* 204 */  {108, 108, 236, 12, 236, 108, 108, 108},
        /* 205 */  {0, 0, 255, 0, 255, 0, 0, 0},
        /* 206 */  {108, 108, 239, 0, 239, 108, 108, 108},
        /* 207 */  {24, 24, 255, 0, 255, 0, 0, 0},
        /* 208 */  {108, 108, 108, 108, 255, 0, 0, 0},
        /* 209 */  {0, 0, 255, 0, 255, 24, 24, 24},
        /* 210 */  {0, 0, 0, 0, 255, 108, 108, 108},
        /* 211 */  {108, 108, 108, 108, 252, 0, 0, 0},
        /* 212 */  {24, 24, 248, 24, 248, 0, 0, 0},
        /* 213 */  {0, 0, 248, 24, 248, 24, 24, 24},
        /* 214 */  {0, 0, 0, 0, 252, 108, 108, 108},
        /* 215 */  {108, 108, 108, 108, 255, 108, 108, 108},
        /* 216 */  {24, 24, 255, 24, 255, 24, 24, 24},
        /* 217 */  {24, 24, 24, 24, 31, 0, 0, 0},
        /* 218 */  {0, 0, 0, 0, 248, 24, 24, 24},
        /* 219 */  {255, 255, 255, 255, 255, 255, 255, 255},
        /* 220 */  {0, 0, 0, 0, 255, 255, 255, 255},
        /* 221 */  {15, 15, 15, 15, 15, 15, 15, 15},
        /* 222 */  {240, 240, 240, 240, 240, 240, 240, 240},
        /* 223 */  {255, 255, 255, 255, 0, 0, 0, 0},
        /* 224 */  {0, 0, 110, 59, 19, 59, 110, 0},
        /* 225 */  {0, 30, 51, 31, 51, 31, 3, 3},
        /* 226 */  {0, 63, 51, 3, 3, 3, 3, 0},
        /* 227 */  {0, 127, 54, 54, 54, 54, 54, 0},
        /* 228 */  {63, 51, 6, 12, 6, 51, 63, 0},
        /* 229 */  {0, 0, 126, 27, 27, 27, 14, 0},
        /* 230 */  {0, 102, 102, 102, 102, 62, 6, 3},
        /* 231 */  {0, 110, 59, 24, 24, 24, 24, 0},
        /* 232 */  {63, 12, 30, 51, 51, 30, 12, 63},
        /* 233 */  {28, 54, 99, 127, 99, 54, 28, 0},
        /* 234 */  {28, 54, 99, 99, 54, 54, 119, 0},
        /* 235 */  {56, 12, 24, 62, 51, 51, 30, 0},
        /* 236 */  {0, 0, 126, 219, 219, 126, 0, 0},
        /* 237 */  {96, 48, 126, 219, 219, 126, 6, 3},
        /* 238 */  {28, 6, 3, 31, 3, 6, 28, 0},
        /* 239 */  {30, 51, 51, 51, 51, 51, 51, 0},
        /* 240 */  {0, 63, 0, 63, 0, 63, 0, 0},
        /* 241 */  {12, 12, 63, 12, 12, 0, 63, 0},
        /* 242 */  {6, 12, 24, 12, 6, 0, 63, 0},
        /* 243 */  {24, 12, 6, 12, 24, 0, 63, 0},
        /* 244 */  {112, 216, 216, 24, 24, 24, 24, 24},
        /* 245 */  {24, 24, 24, 24, 24, 27, 27, 14},
        /* 246 */  {12, 12, 0, 63, 0, 12, 12, 0},
        /* 247 */  {0, 110, 59, 0, 110, 59, 0, 0},
        /* 248 */  {28, 54, 54, 28, 0, 0, 0, 0},
        /* 249 */  {0, 0, 0, 24, 24, 0, 0, 0},
        /* 250 */  {0, 0, 0, 0, 24, 0, 0, 0},
        /* 251 */  {240, 48, 48, 48, 55, 54, 60, 56},
        /* 252 */  {30, 54, 54, 54, 54, 0, 0, 0},
        /* 253 */  {14, 24, 12, 6, 30, 0, 0, 0},
        /* 254 */  {0, 0, 60, 60, 60, 60, 0, 0},
        /* 255 */  {0, 0, 0, 0, 0, 0, 0, 0},
    };

    /**
     * A static non-malleable MinecraftFont.
     */
    public static final MinecraftFont Font = new MinecraftFont(false);

    /**
     * Initialize a new MinecraftFont.
     */
    public MinecraftFont() {
        this(true);
    }

    private MinecraftFont(boolean malleable) {
        for (int i = 1; i < fontData.length; ++i) {
            char ch = (char) i;
            if (i >= 32 && i < 32 + fontChars.length()) {
                ch = fontChars.charAt(i - 32);
            }

            if (ch == ' ') {
                setChar(ch, new CharacterSprite(spaceSize, 8, new boolean[spaceSize * 8]));
                continue;
            }

            int[] rows = fontData[i];
            int width = 0;
            for (int r = 0; r < 8; ++r) {
                for (int c = 0; c < 8; ++c) {
                    if ((rows[r] & (1 << c)) != 0 && c > width) {
                        width = c;
                    }
                }
            }
            ++width;

            boolean[] data = new boolean[width * 8];
            for (int r = 0; r < 8; ++r) {
                for (int c = 0; c < width; ++c) {
                    data[r * width + c] = (rows[r] & (1 << c)) != 0;
                }
            }

            setChar(ch, new CharacterSprite(width, 8, data));
        }

        this.malleable = malleable;
    }

}
