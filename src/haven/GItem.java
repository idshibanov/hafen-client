/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.*;

public class GItem extends AWidget implements ItemInfo.SpriteOwner, GSprite.Owner {
    public Indir<Resource> res;
    public MessageBuf sdt;
    public int meter = 0;
    public int num = -1;
    private GSprite spr;
    private Object[] rawinfo;
    private List<ItemInfo> info = Collections.emptyList();
    public static final Color essenceclr = new Color(202, 110, 244);
    public static final Color substanceclr = new Color(208, 189, 44);
    public static final Color vitalityclr = new Color(157, 201, 72);
    private Quality quality;

    public class Quality {
        public int max;
        public Tex etex, stex, vtex;
        public Tex maxtex, avgtex, avgwholetex, lpgaintex;
        public boolean curio;

        public Quality(int e, int s, int v, boolean curio) {
            this.curio = curio;

            Color color;
            if (e == s && e == v) {
                max = e;
                color = Color.WHITE;
            } else if (e >= s && e >= v) {
                max = e;
                color = essenceclr;
            } else if (s >= e && s >= v) {
                max = s;
                color = substanceclr;
            } else {
                max = v;
                color = vitalityclr;
            }

            double avg =  (double)(e + s + v)/3.0;
            double lpgain = curio ? Math.sqrt(Math.sqrt((double)(e * e + s * s + v * v) / 300.0)) : 0;

            etex = Text.renderstroked(e + "", essenceclr, Color.BLACK).tex();
            stex = Text.renderstroked(s + "", substanceclr, Color.BLACK).tex();
            vtex = Text.renderstroked(v + "", vitalityclr, Color.BLACK).tex();
            maxtex = Text.renderstroked(max + "", color, Color.BLACK).tex();
            avgtex = Text.renderstroked(new DecimalFormat("#.#").format(avg), color, Color.BLACK).tex();
            lpgaintex = Text.renderstroked(new DecimalFormat("#.###").format(lpgain), Color.WHITE, Color.BLACK).tex();
            avgwholetex = Text.renderstroked(Math.round(avg) + "", color, Color.BLACK).tex();
        }
    }

    @RName("item")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            int res = (Integer) args[0];
            Message sdt = (args.length > 1) ? new MessageBuf((byte[]) args[1]) : Message.nil;
            return (new GItem(parent.ui.sess.getres(res), sdt));
        }
    }

    public interface ColorInfo {
        public Color olcol();
    }

    public interface NumberInfo {
        public int itemnum();
    }

    public class Amount extends ItemInfo implements NumberInfo {
        private final int num;

        public Amount(int num) {
            super(GItem.this);
            this.num = num;
        }

        public int itemnum() {
            return (num);
        }
    }

    public GItem(Indir<Resource> res, Message sdt) {
        this.res = res;
        this.sdt = new MessageBuf(sdt);
    }

    public GItem(Indir<Resource> res) {
        this(res, Message.nil);
    }

    private Random rnd = null;

    public Random mkrandoom() {
        if (rnd == null)
            rnd = new Random();
        return (rnd);
    }

    public Resource getres() {
        return (res.get());
    }

    public Glob glob() {
        return (ui.sess.glob);
    }

    public GSprite spr() {
        GSprite spr = this.spr;
        if (spr == null) {
            try {
                spr = this.spr = GSprite.create(this, res.get(), sdt.clone());
            } catch (Loading l) {
            }
        }
        return (spr);
    }

    public void tick(double dt) {
        GSprite spr = spr();
        if (spr != null)
            spr.tick(dt);
    }

    public List<ItemInfo> info() {
        if (info == null)
            info = ItemInfo.buildinfo(this, rawinfo);
        return (info);
    }

    public Resource resource() {
        return (res.get());
    }

    public GSprite sprite() {
        if (spr == null)
            throw (new Loading("Still waiting for sprite to be constructed"));
        return (spr);
    }

    public void uimsg(String name, Object... args) {
        if (name == "num") {
            num = (Integer) args[0];
        } else if (name == "chres") {
            synchronized (this) {
                res = ui.sess.getres((Integer) args[0]);
                sdt = (args.length > 1) ? new MessageBuf((byte[]) args[1]) : MessageBuf.nil;
                spr = null;
            }
        } else if (name == "tt") {
            info = null;
            rawinfo = args;
        } else if (name == "meter") {
            meter = (Integer) args[0];
        }
    }

    public void qualityCalc() {
        int e = 0, s = 0, v = 0;
        boolean curio = false;
        try {
            for (ItemInfo info : info()) {
                if (info.getClass().getSimpleName().equals("QBuff")) {
                    try {
                        String name = (String) info.getClass().getDeclaredField("name").get(info);
                        int val = (Integer) info.getClass().getDeclaredField("q").get(info);
                        if ("Essence".equals(name))
                            e = val;
                        else if ("Substance".equals(name))
                            s = val;
                        else if ("Vitality".equals(name))
                            v = val;
                    } catch (Exception ex) {
                    }
                } else if (info.getClass() == Curiosity.class) {
                    curio = true;
                }
            }
            quality = new Quality(e, s, v, curio);
        } catch (Exception ex) {
        }
    }

    public Quality quality() {
        if (quality == null)
            qualityCalc();
        return quality;
    }
}
