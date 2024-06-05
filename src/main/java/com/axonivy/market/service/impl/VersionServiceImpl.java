package com.axonivy.market.service.impl;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.utils.LatestVersionComparator;
import com.axonivy.market.utils.XmlReader;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class VersionServiceImpl implements VersionService {
    private final GHAxonIvyMarketRepoService gitHubService;

    public VersionServiceImpl(GHAxonIvyMarketRepoService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public static List<MavenArtifact> getMavenArtifacts(List<MavenArtifact> artifacts) {
        return Optional.ofNullable(artifacts).orElse(Collections.emptyList()).stream().filter(product -> !product.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
    }

    //TODO: need to rework this method
    @Override
    public List<String> getVersionsToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) {
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            //TODO: hanlde dev version
        }
        List<String> versions = gitHubService.getTagsFromRepoName(productId).stream().map(GHTag::getName).toList();
        if (StringUtils.isNotBlank(designerVersion)) {
            //TODO: handle it please
            return versions.stream().filter(version -> version.contains(designerVersion)).toList();
        }
        return versions;
    }

    @Override
    public List<String> getVersionsFromProduct(Product product, Boolean isShowDevVersion, String designerVersion) {
        List<String> versions = getVersionsFromMaven(product);
        Stream<String> versionStream = versions.stream();
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            return versionStream.filter(version -> isReleasedVersionOrUnReleaseDevVersion(versions, version)).sorted(new LatestVersionComparator()).toList();
        }
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).toList();
        }

        return versionStream.filter(this::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
    }

    private List<String> getVersionsFromMaven(Product product) {
        List<MavenArtifact> productArtifact = Collections.emptyList();
        Set<String> versions = new HashSet<>();
        for (MavenArtifact artifact : productArtifact) {
            versions.addAll(getVersionsFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(), artifact.getArtifactId()));
            Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).forEach(archivedArtifact -> versions.addAll(getVersionsFromArtifactInfo(artifact.getRepoUrl(), archivedArtifact.getGroupId(), archivedArtifact.getArtifactId())));
        }
        List<String> versionList = new ArrayList<>(versions);
        versionList.sort(new LatestVersionComparator());
        return versionList;
    }

    @Override
    public List<String> getVersionsFromArtifactInfo(String repoUrl, String groupId, String artifactID) {
        List<String> versions = new ArrayList<>();
        String baseUrl = buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactID);
        if (StringUtils.isNotBlank(baseUrl)) {
            versions.addAll(XmlReader.readXMLFromUrl(baseUrl));
        }
        return versions;
    }

    @Override
    public String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
        if (StringUtils.isAnyBlank(groupId, artifactID)) {
            return StringUtils.EMPTY;
        }
        repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
        groupId = groupId.replace(MavenConstants.GROUP_ID_SEPARATOR, MavenConstants.GROUP_ID_URL_SEPARATOR);
        return String.format(MavenConstants.METADATA_URL_FORMAT, repoUrl, groupId, artifactID);
    }

    @Override
    public boolean isReleasedVersionOrUnReleaseDevVersion(List<String> versions, String version) {
        if (isSnapshotVersion(version)) {
            return !versions.contains(version.replace(MavenConstants.SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY));
        } else if (isSprintVersion(version)) {
            return !versions.contains(version.split(MavenConstants.SPRINT_RELEASE_POSTFIX)[0]);
        }
        return true;
    }

    @Override
    public boolean isSnapshotVersion(String version) {
        return version.endsWith(MavenConstants.SNAPSHOT_RELEASE_POSTFIX);
    }

    @Override
    public boolean isSprintVersion(String version) {
        return version.contains(MavenConstants.SPRINT_RELEASE_POSTFIX);
    }

    @Override
    public boolean isReleasedVersion(String version) {
        return !(isSprintVersion(version) || isSnapshotVersion(version));
    }

    private boolean isMatchWithDesignerVersion(String version, String designerVersion) {
        return isReleasedVersion(version) && version.startsWith(designerVersion);
    }

    @Override
    public Map<String, List<String>> getArtifactsToDisplay(String productId) {
        List<String> versions = gitHubService.getTagsFromRepoName(productId).stream().map(GHTag::getName).toList();

        return null;
    }
}
