/*
 * Copyright (c) 2008-2011, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.utils.ParameterStringParser;
import de.matthiasmann.twl.utils.TextUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Style which is constructed from a CSS style string.
 * 
 * @author Matthias Mann
 */
public class CSSStyle extends Style {

    protected CSSStyle() {
    }

    public CSSStyle(String cssStyle) {
        parseCSS(cssStyle);
    }

    public CSSStyle(Style parent, StyleSheetKey styleSheetKey, String cssStyle) {
        super(parent, styleSheetKey);
        parseCSS(cssStyle);
    }

    private void parseCSS(String style) {
        ParameterStringParser psp = new ParameterStringParser(style, ';', ':');
        psp.setTrim(true);
        while(psp.next()) {
            try {
                parseCSSAttribute(psp.getKey(), psp.getValue());
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(CSSStyle.class.getName()).log(Level.SEVERE,
                        "Unable to parse CSS attribute: " + psp.getKey() + "=" + psp.getValue(), ex);
            }
        }
    }

    protected void parseCSSAttribute(String key, String value) {
        if(key.startsWith("margin")) {
            parseBox(key.substring(6), value, StyleAttribute.MARGIN);
            return;
        }
        if(key.startsWith("padding")) {
            parseBox(key.substring(7), value, StyleAttribute.PADDING);
            return;
        }
        if("text-indent".equals(key)) {
            parseValueUnit(StyleAttribute.TEXT_INDENT, value);
            return;
        }
        if("font-family".equals(key) || "font".equals(key) || "-twl-font".equals(key)) {
            put(StyleAttribute.FONT_NAME, stripQuotes(value.trim()));
            return;
        }
        if("text-align".equals(key)) {
            parseEnum(StyleAttribute.HORIZONTAL_ALIGNMENT, value);
            return;
        }
        if("vertical-align".equals(key)) {
            parseEnum(StyleAttribute.VERTICAL_ALIGNMENT, value);
            return;
        }
        if("white-space".equals(key)) {
            parseEnum(StyleAttribute.PREFORMATTED, PRE, value);
            return;
        }
        if("word-wrap".equals(key)) {
            parseEnum(StyleAttribute.BREAKWORD, BREAKWORD, value);
            return;
        }
        if("list-style-image".equals(key)) {
            parseURL(StyleAttribute.LIST_STYLE_IMAGE, value);
            return;
        }
        if("list-style-type".equals(key)) {
            parseEnum(StyleAttribute.LIST_STYLE_TYPE, OLT, value);
            return;
        }
        if("clear".equals(key)) {
            parseEnum(StyleAttribute.CLEAR, value);
            return;
        }
        if("float".equals(key)) {
            parseEnum(StyleAttribute.FLOAT_POSITION, value);
            return;
        }
        if("display".equals(key)) {
            parseEnum(StyleAttribute.DISPLAY, value);
            return;
        }
        if("width".equals(key)) {
            parseValueUnit(StyleAttribute.WIDTH, value);
            return;
        }
        if("height".equals(key)) {
            parseValueUnit(StyleAttribute.HEIGHT, value);
            return;
        }
        if("background-image".equals(key)) {
            parseURL(StyleAttribute.BACKGROUND_IMAGE, value);
            return;
        }
        if("background-color".equals(key) || "-twl-background-color".equals(key)) {
            parseColor(StyleAttribute.BACKGROUND_COLOR, value);
            return;
        }
        if("color".equals(key)) {
            parseColor(StyleAttribute.COLOR, value);
            return;
        }
        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    private void parseBox(String key, String value, BoxAttribute box) {
        if("-top".equals(key)) {
            parseValueUnit(box.top, value);
        } else if("-left".equals(key)) {
            parseValueUnit(box.left, value);
        } else if("-right".equals(key)) {
            parseValueUnit(box.right, value);
        } else if("-bottom".equals(key)) {
            parseValueUnit(box.bottom, value);
        } else if("".equals(key)) {
            Value[] vu = parseValueUnits(value);
            switch(vu.length) {
                case 1:
                    put(box.top, vu[0]);
                    put(box.left, vu[0]);
                    put(box.right, vu[0]);
                    put(box.bottom, vu[0]);
                    break;
                case 2: // TB, LR
                    put(box.top, vu[0]);
                    put(box.left, vu[1]);
                    put(box.right, vu[1]);
                    put(box.bottom, vu[0]);
                    break;
                case 3: // T, LR, B
                    put(box.top, vu[0]);
                    put(box.left, vu[1]);
                    put(box.right, vu[1]);
                    put(box.bottom, vu[2]);
                    break;
                case 4: // T, R, B, L
                    put(box.top, vu[0]);
                    put(box.left, vu[3]);
                    put(box.right, vu[1]);
                    put(box.bottom, vu[2]);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid number of margin values: " + vu.length);
            }
        }
    }

    private Value parseValueUnit(String value) {
        Value.Unit unit;
        int suffixLength = 2;
        if(value.endsWith("px")) {
            unit = Value.Unit.PX;
        } else if(value.endsWith("em")) {
            unit = Value.Unit.EM;
        } else if(value.endsWith("ex")) {
            unit = Value.Unit.EX;
        } else if(value.endsWith("%")) {
            suffixLength = 1;
            unit = Value.Unit.PERCENT;
        } else if("0".equals(value)) {
            return Value.ZERO_PX;
        } else if("auto".equals(value)) {
            return Value.AUTO;
        } else {
            throw new IllegalArgumentException("Unknown numeric suffix: " + value);
        }

        String numberPart = value.substring(0, value.length() - suffixLength).trim();
        return new Value(Float.parseFloat(numberPart), unit);
    }

    private Value[] parseValueUnits(String value) {
        String[] parts = value.split("\\s+");
        Value[] result = new Value[parts.length];
        for(int i=0 ; i<parts.length ; i++) {
            result[i] = parseValueUnit(parts[i]);
        }
        return result;
    }

    private void parseValueUnit(StyleAttribute<?> attribute, String value) {
        put(attribute, parseValueUnit(value));
    }

    private<T> void parseEnum(StyleAttribute<T> attribute, HashMap<String, T> map, String value) {
        T obj = map.get(value);
        if(obj == null) {
            throw new IllegalArgumentException("Unknown value: " + value);
        }
        put(attribute, obj);
    }

    private<E extends Enum<E>> void parseEnum(StyleAttribute<E> attribute, String value) {
        E obj = Enum.valueOf(attribute.getDataType(), value.toUpperCase(Locale.ENGLISH));
        put(attribute, obj);
    }

    private void parseURL(StyleAttribute<String> attribute, String value) {
        if(value.startsWith("url(") && value.endsWith(")")) {
            value = value.substring(4, value.length() - 1).trim();
            value = stripQuotes(value);
        }
        put(attribute, value);
    }

    private String stripQuotes(String value) {
        if((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private void parseColor(StyleAttribute<Color> attribute, String value) {
        Color color;
        if(value.startsWith("rgb(") && value.endsWith(")")) {
            value = value.substring(4, value.length() - 1).trim();
            byte[] rgb = parseRGBA(value, 3);
            color = new Color(rgb[0], rgb[1], rgb[2], (byte)255);
        } else if(value.startsWith("rgba(") && value.endsWith(")")) {
            value = value.substring(5, value.length() - 1).trim();
            byte[] rgba = parseRGBA(value, 4);
            color = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
        } else {
            color = Color.parserColor(value);
            if(color == null) {
                throw new IllegalArgumentException("unknown color name: " + value);
            }
        }
        put(attribute, color);
    }
    
    private byte[] parseRGBA(String value, int numElements) {
        String[] parts = value.split(",");
        if(parts.length != numElements) {
            throw new IllegalArgumentException("3 values required for rgb()");
        }
        byte[] rgba = new byte[numElements];
        for(int i=0 ; i<numElements ; i++) {
            String part = parts[i].trim();
            int v;
            if(i == 3) {
                // handle alpha component specially
                float f = Float.parseFloat(part);
                v = Math.round(f * 255.0f);
            } else {
                boolean percent = part.endsWith("%");
                if(percent) {
                    part = part.substring(0, part.length()-1).trim();
                }
                v = Integer.parseInt(part);
                if(percent) {
                    v = 255*v / 100;
                }
            }
            rgba[i] = (byte)Math.max(0, Math.min(255, v));
        }
        return rgba;
    }
    
    static final HashMap<String, Boolean> PRE = new HashMap<String, Boolean>();
    static final HashMap<String, Boolean> BREAKWORD = new HashMap<String, Boolean>();
    static final HashMap<String, OrderedListType> OLT = new HashMap<String, OrderedListType>();

    static OrderedListType createRoman(final boolean lowercase) {
        return new OrderedListType() {
            @Override
            public String format(int nr) {
                if(nr >= 1 && nr <= TextUtil.MAX_ROMAN_INTEGER) {
                    String str = TextUtil.toRomanNumberString(nr);
                    return lowercase ? str.toLowerCase() : str;
                } else {
                    return Integer.toString(nr);
                }
            }
        };
    }
    
    static {
        PRE.put("pre", Boolean.TRUE);
        PRE.put("normal", Boolean.FALSE);

        BREAKWORD.put("normal", Boolean.FALSE);
        BREAKWORD.put("break-word", Boolean.TRUE);

        OrderedListType upper_alpha = new OrderedListType("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        OrderedListType lower_alpha = new OrderedListType("abcdefghijklmnopqrstuvwxyz");
        OLT.put("decimal", OrderedListType.DECIMAL);
        OLT.put("upper-alpha", upper_alpha);
        OLT.put("lower-alpha", lower_alpha);
        OLT.put("upper-latin", upper_alpha);
        OLT.put("lower-latin", lower_alpha);
        OLT.put("upper-roman", createRoman(false));
        OLT.put("lower-roman", createRoman(true));
        OLT.put("lower-greek", new OrderedListType("αβγδεζηθικλμνξοπρστυφχψω"));
        OLT.put("upper-norwegian", new OrderedListType("ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ"));
        OLT.put("lower-norwegian", new OrderedListType("abcdefghijklmnopqrstuvwxyzæøå"));
        OLT.put("upper-russian-short", new OrderedListType("АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ"));
        OLT.put("lower-russian-short", new OrderedListType("абвгдежзиклмнопрстуфхцчшщэюя"));
    }
}
