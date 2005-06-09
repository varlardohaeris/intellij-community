package com.intellij.psi.formatter.xml;

import com.intellij.newCodeFormatting.Block;
import com.intellij.newCodeFormatting.ChildAttributes;
import com.intellij.newCodeFormatting.Indent;
import com.intellij.newCodeFormatting.SpaceProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SyntheticBlock extends AbstractSyntheticBlock implements Block{
  private final List<Block> mySubBlocks;
  private final Indent myChildIndent;

  SyntheticBlock(final List<Block> subBlocks, final Block parent, final Indent indent, XmlFormattingPolicy policy, final Indent childIndent) {
    super(subBlocks, parent, policy, indent);
    mySubBlocks = subBlocks;
    myChildIndent = childIndent;
  }

  @NotNull
  public TextRange getTextRange() {
    return calculateTextRange(mySubBlocks);
  }

  @NotNull
  public List<Block> getSubBlocks() {
    return mySubBlocks;
  }

  public SpaceProperty getSpaceProperty(Block child1, Block child2) {
    if (child1 instanceof ReadOnlyBlock || child2 instanceof ReadOnlyBlock) {
      return getFormatter().getReadOnlySpace();
    }
    if (!(child1 instanceof AbstractXmlBlock) || !(child2 instanceof AbstractXmlBlock)) {
      return null;
    }
    final IElementType type1 = ((AbstractBlock)child1).getNode().getElementType();
    final IElementType type2 = ((AbstractBlock)child2).getNode().getElementType();

    if (isXmlTagName(type1, type2)){
      final int spaces = myXmlFormattingPolicy.getShouldAddSpaceAroundTagName() ? 1 : 0;
      return getFormatter().createSpaceProperty(spaces, spaces, 0, myXmlFormattingPolicy.getShouldKeepLineBreaks(), myXmlFormattingPolicy.getKeepBlankLines());
    } else if (type2 == ElementType.XML_ATTRIBUTE) {
      return getFormatter().createSpaceProperty(1, 1, 0, myXmlFormattingPolicy.getShouldKeepLineBreaks(), myXmlFormattingPolicy.getKeepBlankLines());
    } else if (((AbstractXmlBlock)child1).isTextElement() && ((AbstractXmlBlock)child2).isTextElement()) {
      return getFormatter().createSafeSpace(myXmlFormattingPolicy.getShouldKeepLineBreaks(), myXmlFormattingPolicy.getKeepBlankLines());
    } else if (type2 == getTagType() && (type1 == ElementType.XML_DATA_CHARACTERS || type1== getTagType())
               && ((AbstractXmlBlock)child2).insertLineBreakBeforeTag()) {
      //<tag/>text <tag/></tag>
      return getFormatter().createSpaceProperty(0, Integer.MAX_VALUE, 2, myXmlFormattingPolicy.getShouldKeepLineBreaks(),
                                                myXmlFormattingPolicy.getKeepBlankLines());
    }
    else if (type2 == getTagType() && (type1 == ElementType.XML_DATA_CHARACTERS || type1== getTagType())
             && ((AbstractXmlBlock)child2).removeLineBreakBeforeTag()) {
      //<tag/></tag> text</tag>
      return getFormatter().createSpaceProperty(0, Integer.MAX_VALUE, 0, myXmlFormattingPolicy.getShouldKeepLineBreaks(),
                                                myXmlFormattingPolicy.getKeepBlankLines());
    }

    if (type1 == getTagType() && type2 == ElementType.XML_DATA_CHARACTERS) {     //<tag/>-text
      if (((AbstractXmlBlock)child1).isTextElement()) {
        return getFormatter().createSafeSpace(myXmlFormattingPolicy.getShouldKeepLineBreaks(), myXmlFormattingPolicy.getKeepBlankLines());
      } else {
        return getFormatter().createSpaceProperty(0, 0, 0, true, myXmlFormattingPolicy.getKeepBlankLines());
      }
    }

    if (type2 == getTagType() && type1 == ElementType.XML_DATA_CHARACTERS) {     //text-<tag/>
      if (((AbstractXmlBlock)child2).isTextElement()) {
        return getFormatter().createSafeSpace(true, myXmlFormattingPolicy.getKeepBlankLines());
      } else {
        return getFormatter().createSpaceProperty(0, 0, 0, true, myXmlFormattingPolicy.getKeepBlankLines());
      }
    }
    if (type2 == getTagType() && type1== getTagType()) {//<tag/><tag/>
      return getFormatter().createSpaceProperty(0, Integer.MAX_VALUE, 0, true,
                                                myXmlFormattingPolicy.getKeepBlankLines());
    }

    return getFormatter().createSpaceProperty(0, Integer.MAX_VALUE, 0, myXmlFormattingPolicy.getShouldKeepLineBreaks(), myXmlFormattingPolicy.getKeepBlankLines());
  }

  @NotNull
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    return new ChildAttributes(myChildIndent, null);
  }

  public boolean isIncomplete() {
    return getSubBlocks().get(getSubBlocks().size() - 1).isIncomplete();
  }

}
