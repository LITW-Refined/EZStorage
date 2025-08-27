package com.zerofall.ezstorage.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.zerofall.ezstorage.integration.ModIds;

import codechicken.nei.util.ReadableNumberConverter;

public class EZItemRenderer extends RenderItem {

    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
        if (stack != null) {
            float ScaleFactor = 0.5f;
            float RScaleFactor = 1.0f / ScaleFactor;
            int offset = 0;

            boolean unicodeFlag = fr.getUnicodeFlag();
            fr.setUnicodeFlag(false);

            long amount = Long.parseLong(text);

            if (amount > 999999999999L) amount = 999999999999L;

            if (stack.getItem()
                .showDurabilityBar(stack)) {
                double health = stack.getItem()
                    .getDurabilityForDisplay(stack);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                Tessellator tessellator = Tessellator.instance;
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 0x3F00;
                renderQuad(tessellator, xPosition + 2, yPosition + 13, 13, 2, 0);
                renderQuad(tessellator, xPosition + 2, yPosition + 13, 12, 1, i1);
                renderQuad(tessellator, xPosition + 2, yPosition + 13, j1, 1, l);
                // GL11.glEnable(GL11.GL_BLEND); // Forge: Disable Bled because it screws with a lot of things down
                // the line.
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glPushMatrix();
            GL11.glScaled(ScaleFactor, ScaleFactor, ScaleFactor);
            String var6;
            if (ModIds.NEI.isLoaded()) {
                var6 = ReadableNumberConverter.INSTANCE.toWideReadableForm(amount);
            } else {
                var6 = String.valueOf(amount);
            }
            int X = (int) (((float) xPosition + offset + 15.0f - fr.getStringWidth(var6) * ScaleFactor) * RScaleFactor);
            int Y = (int) (((float) yPosition + offset + 15.0f - 7.0f * ScaleFactor) * RScaleFactor);
            fr.drawStringWithShadow(var6, X, Y, 16777215);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            fr.setUnicodeFlag(unicodeFlag);
        }

    }

    private void renderQuad(Tessellator p_77017_1_, int p_77017_2_, int p_77017_3_, int p_77017_4_, int p_77017_5_,
        int p_77017_6_) {
        p_77017_1_.startDrawingQuads();
        p_77017_1_.setColorOpaque_I(p_77017_6_);
        p_77017_1_.addVertex((double) (p_77017_2_ + 0), (double) (p_77017_3_ + 0), 0.0D);
        p_77017_1_.addVertex((double) (p_77017_2_ + 0), (double) (p_77017_3_ + p_77017_5_), 0.0D);
        p_77017_1_.addVertex((double) (p_77017_2_ + p_77017_4_), (double) (p_77017_3_ + p_77017_5_), 0.0D);
        p_77017_1_.addVertex((double) (p_77017_2_ + p_77017_4_), (double) (p_77017_3_ + 0), 0.0D);
        p_77017_1_.draw();
    }
}
