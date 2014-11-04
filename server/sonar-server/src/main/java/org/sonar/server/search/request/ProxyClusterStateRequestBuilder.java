/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.search.request;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.sonar.core.profiling.Profiling;
import org.sonar.core.profiling.StopWatch;
import org.sonar.server.search.SearchClient;

public class ProxyClusterStateRequestBuilder extends ClusterStateRequestBuilder {

  private final Profiling profiling;

  public ProxyClusterStateRequestBuilder(SearchClient client, Profiling profiling) {
    super(client.admin().cluster());
    this.profiling = profiling;
  }

  @Override
  public ClusterStateResponse get() {
    StopWatch fullProfile = profiling.start("cluster state", Profiling.Level.FULL);
    try {
      return super.execute().actionGet();
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Fail to execute %s", toString()), e);
    } finally {
      if (profiling.isProfilingEnabled(Profiling.Level.BASIC)) {
        fullProfile.stop("%s", toString());
      }
    }
  }

  @Override
  public ClusterStateResponse get(TimeValue timeout) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ClusterStateResponse get(String timeout) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ListenableActionFuture<ClusterStateResponse> execute() {
    throw new UnsupportedOperationException("execute() should not be called as it's used for asynchronous");
  }

  @Override
  public String toString() {
    StringBuilder message = new StringBuilder();
    message.append("ES cluster state request");
    if (request.indices().length > 0) {
      message.append(String.format(" on indices '%s'", StringUtils.join(request.indices(), ",")));
    }
    return message.toString();
  }
}
