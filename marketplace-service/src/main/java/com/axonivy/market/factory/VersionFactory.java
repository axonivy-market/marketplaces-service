package com.axonivy.market.factory;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.enums.DevelopmentVersion;
import com.axonivy.market.util.VersionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.MavenConstants.DEV_RELEASE_POSTFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory {

  public static String get(List<String> versions, String requestedVersion) {
    var sortedVersions = Optional.ofNullable(versions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull)
        .sorted((v1, v2) -> MavenVersionComparator.compare(v2, v1)).toList();
    // Redirect to the newest version for special keywords
    var version = DevelopmentVersion.of(requestedVersion);
    if (version != null) {
      return sortedVersions.get(0);
    }

    // e.g. 10.0-dev
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
    }
    return findVersionStartWith(sortedVersions, requestedVersion);
  }

  public static String getFromMetadata(List<Metadata> metadataList, String requestedVersion) {
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest dev version from metadata
    if (Objects.nonNull(version) && version != DevelopmentVersion.LATEST) {
      return metadataList.stream().map(Metadata::getLatest).min(new LatestVersionComparator()).orElse(EMPTY);
    }

    List<String> artifactVersions = metadataList.stream().flatMap(metadata -> metadata.getVersions().stream()).sorted(
        new LatestVersionComparator()).toList();
    List<String> releasedVersions = artifactVersions.stream().filter(VersionUtils::isReleasedVersion).sorted(
        new LatestVersionComparator()).toList();

    // Get latest released version from metadata
    if (version == DevelopmentVersion.LATEST) {
      return releasedVersions.stream().min(new LatestVersionComparator()).orElse(EMPTY);
    }

    // Get latest dev version from specific version
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
      return findVersionStartWith(artifactVersions, requestedVersion);
    }

    String matchVersion = findVersionStartWith(releasedVersions, requestedVersion);

    // Return latest version of specific version if can not fnd latest release of that version
    if ((VersionUtils.isMajorVersion(requestedVersion) || VersionUtils.isMinorVersion(
        requestedVersion)) && !CollectionUtils.containsInstance(releasedVersions, matchVersion)) {
      return findVersionStartWith(artifactVersions, requestedVersion);
    }
    return matchVersion;
  }

  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    return CollectionUtils.isEmpty(releaseVersions) ? version : releaseVersions.stream().filter(
        ver -> ver.startsWith(version)).findAny().orElse(version);
  }
}
