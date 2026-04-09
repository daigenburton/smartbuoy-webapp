package edu.bu.web;

import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import edu.bu.data.Deployment;
import edu.bu.web.dto.DeploymentRequest;
import edu.bu.web.dto.DeploymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** REST controller handling buoy deployment configuration requests. */
@RestController
public class DeploymentController {

  private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);

  private final DataStore dataStore;

  /** Creates a DeploymentController backed by the given DataStore. */
  public DeploymentController(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /** Deploys a buoy by recording its current GPS position as the geofence center. */
  @PostMapping("/deploy")
  public DeploymentResponse deploy(@RequestBody DeploymentRequest request)
      throws UnknownBuoyException {

    int buoyId = request.getBuoyId();
    BuoyResponse latest =
        dataStore
            .getLatest(buoyId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Device has not reported GPS data yet. Please place buoy in water and wait for signal."));

    double lat = latest.getLatitude();
    double lon = latest.getLongitude();
    double radius = request.getAllowedRadiusMeters();
    String userId = request.getUserId();

    Deployment deployment = new Deployment(buoyId, lat, lon, radius, System.currentTimeMillis(), userId);
    dataStore.saveDeployment(deployment);

    log.info("Deployment saved for buoy {}", buoyId);
    return new DeploymentResponse(buoyId, lat, lon, radius);
  }
}
