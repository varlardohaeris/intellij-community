package com.intellij.dupLocator;

import com.intellij.dupLocator.treeHash.FragmentsCollector;
import com.intellij.lang.Language;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DuplicatesProfileCache {
  private static final Map<DupInfo, TIntObjectHashMap<DuplicatesProfile>> ourProfileCache = new HashMap<>();

  private DuplicatesProfileCache() {
  }

  public static void clear(@NotNull DupInfo info) {
    ourProfileCache.remove(info);
  }

  @Nullable
  public static DuplicatesProfile getProfile(@NotNull DupInfo dupInfo, int index) {
    TIntObjectHashMap<DuplicatesProfile> patternCache = ourProfileCache.get(dupInfo);
    if (patternCache == null) {
      patternCache = new TIntObjectHashMap<>();
      ourProfileCache.put(dupInfo, patternCache);
    }
    DuplicatesProfile result = patternCache.get(index);
    if (result == null) {
      DuplicatesProfile theProfile = null;
      for (DuplicatesProfile profile : DuplicatesProfile.EP_NAME.getExtensionList()) {
        if (profile.isMyDuplicate(dupInfo, index)) {
          theProfile = profile;
          break;
        }
      }
      result = theProfile == null ? NULL_PROFILE : theProfile;
      patternCache.put(index, result);
    }
    return result == NULL_PROFILE ? null : result;
  }

  private static final DuplicatesProfile NULL_PROFILE = new DuplicatesProfile() {
    @NotNull
    @Override
    public DuplocateVisitor createVisitor(@NotNull FragmentsCollector collector) {
      return null;
    }

    @Override
    public boolean isMyLanguage(@NotNull Language language) {
      return false;
    }

    @NotNull
    @Override
    public DuplocatorState getDuplocatorState(@NotNull Language language) {
      return null;
    }

    @Override
    public boolean isMyDuplicate(@NotNull DupInfo info, int index) {
      return false;
    }
  };
}
