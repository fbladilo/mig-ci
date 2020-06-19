// parallel-base.groovy
properties([
parameters([choice(choices: ['3.7', '3.9', '3.10', '3.11', '4.1', '4.2', '4.3', 'pre-4.4', 'nightly'], description: 'OCP version to deploy on source cluster', name: 'SRC_CLUSTER_VERSION'),
choice(choices: ['4.3', '3.11', '3.10', '3.9', '3.7', '4.1', '4.2', 'pre-4.4', 'nightly'], description: 'OCP version to deploy on destination cluster', name: 'DEST_CLUSTER_VERSION'),
string(defaultValue: 'jenkins-ci-parallel-base', description: 'Cluster name to deploy', name: 'CLUSTER_NAME', trim: false),
string(defaultValue: 'mig-ci@redhat.com', description: 'Email to register the deployment', name: 'EMAIL', trim: false),
string(defaultValue: '1', description: 'OCP3 master instance count', name: 'OCP3_MASTER_INSTANCE_COUNT', trim: false),
string(defaultValue: '1', description: 'OCP3 worker instance count', name: 'OCP3_WORKER_INSTANCE_COUNT', trim: false),
string(defaultValue: '1', description: 'OCP3 infra instance count', name: 'OCP3_INFRA_INSTANCE_COUNT', trim: false),
string(defaultValue: 'm4.xlarge', description: 'OCP3 master instance type', name: 'OCP3_MASTER_INSTANCE_TYPE', trim: false),
string(defaultValue: 'm4.xlarge', description: 'OCP3 worker instance type', name: 'OCP3_WORKER_INSTANCE_TYPE', trim: false),
string(defaultValue: 'm4.xlarge', description: 'OCP3 infra instance type', name: 'OCP3_INFRA_INSTANCE_TYPE', trim: false),
string(defaultValue: '1', description: 'OCP4 master instance count', name: 'OCP4_MASTER_INSTANCE_COUNT', trim: false),
string(defaultValue: '1', description: 'OCP4 worker instance count', name: 'OCP4_WORKER_INSTANCE_COUNT', trim: false),
string(defaultValue: 'm4.2xlarge', description: 'OCP4 master instance type', name: 'OCP4_MASTER_INSTANCE_TYPE', trim: false),
string(defaultValue: 'm4.xlarge', description: 'OCP4 worker instance type', name: 'OCP4_WORKER_INSTANCE_TYPE', trim: false),
string(defaultValue: 'm4.xlarge', description: 'OCP4 infra instance type', name: 'OCP4_INFRA_INSTANCE_TYPE', trim: false),
string(defaultValue: '.mg.dog8code.com', description: 'Zone suffix for instance hostname address', name: 'BASESUFFIX', trim: false),
string(defaultValue: 'Z2GE8CSGW2ZA8W', description: 'Zone id', name: 'HOSTZONEID', trim: false),
string(defaultValue: 'ci', description: 'EC2 SSH key name to deploy on instances for remote access ', name: 'EC2_KEY', trim: false),
string(defaultValue: 'us-west-1', description: 'AWS region to deploy instances', name: 'AWS_REGION', trim: false),
string(defaultValue: '', description: 'Disconnected config file for source cluster, only used for disconnected deployments', name: 'SRC_DISCONNECTED_CONFIG', trim: false),
string(defaultValue: '', description: 'Disconnected config file for destination cluster, only used for disconnected deployments', name: 'DEST_DISCONNECTED_CONFIG', trim: false),
string(defaultValue: 'https://github.com/konveyor/mig-operator.git', description: 'Mig operator repo to clone', name: 'MIG_OPERATOR_REPO', trim: false),
string(defaultValue: 'master', description: 'Mig operator branch to test', name: 'MIG_OPERATOR_BRANCH', trim: false),
string(defaultValue: 'https://github.com/konveyor/mig-controller.git', description: 'Mig controller repo to test, only used by GHPRB', name: 'MIG_CONTROLLER_REPO', trim: false),
string(defaultValue: 'master', description: 'Mig controller repo branch to test', name: 'MIG_CONTROLLER_BRANCH', trim: false),
string(defaultValue: 'https://github.com/konveyor/mig-e2e.git', description: 'Mig e2e repo to test', name: 'MIG_E2E_REPO', trim: false),
string(defaultValue: 'master', description: 'Mig e2e repo branch to test', name: 'MIG_E2E_BRANCH', trim: false),
string(defaultValue: 'e2e_mig_samples.yml', description: 'e2e test playbook to run, see https://github.com/konveyor/mig-e2e for details', name: 'E2E_PLAY', trim: false),
string(defaultValue: 'all', description: 'e2e test tags to run, see https://github.com/konveyor/mig-e2e for details, space delimited', name: 'E2E_TESTS', trim: false),
string(defaultValue: 'latest', description: 'Mig Operator/CAM release to deploy', name: 'MIG_OPERATOR_RELEASE', trim: false),
string(defaultValue: 'scripts/mig_debug.sh', description: 'Relative file path to debug script on MIG CI repo', name: 'DEBUG_SCRIPT', trim: false),
string(defaultValue: '', description: 'Extra debug script arguments', name: 'DEBUG_SCRIPT_ARGS', trim: false),
string(defaultValue: '#forum-mig-ci', description: 'Slack channel to send notification', name: 'SLACK_CHANNEL', trim: false),
string(defaultValue: 'quay.io/fbladilo/mig-controller', description: 'Repo for quay io for custom mig-controller images, only used by GHPRB', name: 'QUAYIO_CI_REPO', trim: false),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.UsernamePasswordMultiBinding', defaultValue: 'ci_quay_credentials', description: 'Credentials for quay.io container storage, used by mig-controller to push and pull images', name: 'QUAYIO_CREDENTIALS', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'agnosticd_own_repo', description: 'Private repo address for openshift-ansible packages', name: 'AGND_REPO', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'ci_aws_access_key_id', description: 'EC2 access key ID for auth purposes', name: 'EC2_ACCESS_KEY_ID', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'ci_aws_secret_access_key', description: 'EC2 private key needed to access instances, from Jenkins credentials store', name: 'EC2_SECRET_ACCESS_KEY', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl', defaultValue: 'ci_ec2_key', description: 'EC2 private key needed to access instances, from Jenkins credentials store', name: 'EC2_PRIV_KEY', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl', defaultValue: 'ci_pub_key', description: 'EC2 public key needed for agnosticd instances', name: 'EC2_PUB_KEY', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'ci_rhel_sub_user', description: 'RHEL Openshift subscription account username', name: 'EC2_SUB_USER', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'ci_rhel_sub_pass', description: 'RHEL Openshift subscription account password', name: 'EC2_SUB_PASS', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'ci_quay_token', description: 'QUAY token needed for disconnected deployments', name: 'QUAY_TOKEN', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl', defaultValue: 'cam_disconnected_repo', description: 'Private repo for disconnected deployments', name: 'CAM_DISCONNECTED_REPO', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl', defaultValue: 'ci_pull_secret', description: 'Pull secret needed for OCP4 deployments', name: 'OCP4_PULL_SECRET', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.UsernamePasswordMultiBinding', defaultValue: 'ci_ocp4_admin_credentials', description: 'Cluster admin credentials used in OCP4 deployments', name: 'OCP4_CREDENTIALS', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.UsernamePasswordMultiBinding', defaultValue: 'ci_ocp3_admin_credentials', description: 'Cluster admin credentials used in OCP3 deployments', name: 'OCP3_CREDENTIALS', required: true),
credentials(credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.UsernamePasswordMultiBinding', defaultValue: 'ci_stage_registry_credentials', description: 'Stage registry credentials used in disconnected deployments', name: 'STAGE_REGISTRY_CREDENTIALS', required: true),
booleanParam(defaultValue: true, description: 'Run e2e tests', name: 'E2E_RUN'),
booleanParam(defaultValue: false, description: 'Deploy e2e applications and prepare clusters only, do not migrate', name: 'E2E_DEPLOY_ONLY'),
booleanParam(defaultValue: true, description: 'Update OCP3 cluster packages to latest', name: 'OCP3_UPDATE'),
booleanParam(defaultValue: true, description: 'Provision glusterfs workload on OCP3', name: 'OCP3_GLUSTERFS'),
booleanParam(defaultValue: true, description: 'Provision OCS ceph/noobaa workload on destination cluster', name: 'OCS'),
booleanParam(defaultValue: true, description: 'Deploy CAM', name: 'DEPLOY_CAM'),
booleanParam(defaultValue: true, description: 'Deploy mig operator using OLM on OCP4', name: 'USE_OLM'),
booleanParam(defaultValue: false, description: 'Deploy using downstream images', name: 'USE_DOWNSTREAM'),
booleanParam(defaultValue: false, description: 'Deploy CAM disconnected, not used on non-deployment pipelines', name: 'USE_DISCONNECTED'),
booleanParam(defaultValue: true, description: 'Clean up workspace after build', name: 'CLEAN_WORKSPACE'),
booleanParam(defaultValue: false, description: 'Persistent cluster builds with fixed hostname', name: 'PERSISTENT'),
booleanParam(defaultValue: false, description: 'Enable debugging', name: 'DEBUG'),
booleanParam(defaultValue: true, description: 'EC2 terminate instances after build', name: 'EC2_TERMINATE_INSTANCES'),
booleanParam(defaultValue: false, description: 'Enable Slack notifications', name: 'NOTIF_ENABLED'),
booleanParam(defaultValue: true, description: 'Deploy mig controller UI on host cluster', name: 'MIG_CONTROLLER_UI')])])

// true/false build parameter that defines if CAM is deployed
def DEPLOY_CAM = params.DEPLOY_CAM
// do not build custom mig-operator image
MIG_OPERATOR_BUILD_CUSTOM = false
// do not build custom mig-controller image
MIG_CONTROLLER_BUILD_CUSTOM = false
// true/false build parameter that defines if we terminate instances once build is done
def EC2_TERMINATE_INSTANCES = params.EC2_TERMINATE_INSTANCES
// true/false build parameter that defines if we cleanup workspace once build is done
def CLEAN_WORKSPACE = params.CLEAN_WORKSPACE
// true/false build parameter that defines if e2e tests are run
def E2E_RUN = params.E2E_RUN
// Split e2e tests from string param
def E2E_TESTS = params.E2E_TESTS.split(' ')
// true/false enable debugging
def DEBUG = params.DEBUG
// true/false build parameter that defines if we use OLM to deploy mig operator on OCP4
USE_OLM = params.USE_OLM
// true/false persistent clusters
PERSISTENT = params.PERSISTENT
// true/false provision glusterfs
OCP3_GLUSTERFS = params.OCP3_GLUSTERFS
// true/false deploy CAM disconnected 
USE_DISCONNECTED = params.USE_DISCONNECTED
// true/false deploy e2e apps only
E2E_DEPLOY_ONLY = params.E2E_DEPLOY_ONLY

def common_stages
def utils

steps_finished = []

echo "Running job ${env.JOB_NAME}, build ${env.BUILD_ID} on ${env.JENKINS_URL}"
echo "Build URL ${env.BUILD_URL}"
echo "Job URL ${env.JOB_URL}"

node {
    sh "mkdir ${WORKSPACE}-${BUILD_NUMBER}"
    ws("${WORKSPACE}-${BUILD_NUMBER}") {
    try {
        checkout scm
        common_stages = load "${WORKSPACE}/pipeline/common_stages.groovy"
        utils = load "${WORKSPACE}/pipeline/utils.groovy"

        utils.notifyBuild('STARTED')

        stage('Setup for source and destination cluster') {
            steps_finished << 'Setup for source and destination cluster'
            utils.prepare_workspace(SRC_CLUSTER_VERSION, DEST_CLUSTER_VERSION)
            utils.prepare_persistent()
            utils.copy_private_keys()
            utils.copy_public_keys()
            utils.clone_mig_e2e()
            utils.prepare_agnosticd()
        }

        if (env.MIG_CONTROLLER_REPO != 'https://github.com/konveyor/mig-controller.git') {
          utils.clone_mig_controller()
          common_stages.build_mig_controller().call()
        }

        stage('Deploy clusters') {
            steps_finished << 'Deploy clusters'
            parallel deploy_src_cluster: {
              if (SRC_IS_OCP3 == 'true') {
                common_stages.deploy_ocp3_agnosticd(SOURCE_KUBECONFIG, SRC_CLUSTER_VERSION).call()
              } else {
                  common_stages.deploy_ocp4_agnosticd(SOURCE_KUBECONFIG, SRC_CLUSTER_VERSION).call()
                }
            }, deploy_dest_cluster: {
                 if (DEST_IS_OCP3 == 'true') {
                 common_stages.deploy_ocp3_agnosticd(TARGET_KUBECONFIG, DEST_CLUSTER_VERSION).call()
              } else {
                  common_stages.deploy_ocp4_agnosticd(TARGET_KUBECONFIG, DEST_CLUSTER_VERSION).call()
                  common_stages.deploy_workload('ocs-operator',DEST_CLUSTER_VERSION,OCS).call()
                    }
            },
            failFast: false
        }
        if (USE_DISCONNECTED) {
            common_stages.cam_disconnected(SOURCE_KUBECONFIG, SRC_DISCONNECTED_CONFIG, SRC_CLUSTER_VERSION, false).call()
            common_stages.cam_disconnected(TARGET_KUBECONFIG, DEST_DISCONNECTED_CONFIG, DEST_CLUSTER_VERSION, true).call()
            sleep 300
        }
        if (DEPLOY_CAM) {
            // deploy mig-operator and mig-controller on source
            common_stages.deploy_mig_operator(SOURCE_KUBECONFIG, false, SRC_CLUSTER_VERSION).call()
            common_stages.deploy_mig_controller(SOURCE_KUBECONFIG, false, SRC_CLUSTER_VERSION).call()

            // deploy mig-operator and mig-controller on destination
            common_stages.deploy_mig_operator(TARGET_KUBECONFIG, true, DEST_CLUSTER_VERSION).call()
            common_stages.deploy_mig_controller(TARGET_KUBECONFIG, true, DEST_CLUSTER_VERSION).call()
        }
        if (E2E_RUN) {
            common_stages.execute_migration(E2E_TESTS, SOURCE_KUBECONFIG, TARGET_KUBECONFIG).call()
        }

    } catch (Exception ex) {
        currentBuild.result = "FAILED"
        println(ex.toString())
    } finally {
        // Success or failure, always send notifications
        utils.notifyBuild(currentBuild.result)
        if (DEBUG) {
          utils.run_debug(SOURCE_KUBECONFIG, 'Source')
          utils.run_debug(TARGET_KUBECONFIG, 'Destination')
	}

        stage('Clean Up Environment') {
          if (EC2_TERMINATE_INSTANCES) {
            parallel destroy_src_cluster: {
              utils.teardown_ocp_agnosticd(SRC_CLUSTER_VERSION)
            }, destroy_dest_cluster: {
              utils.teardown_ocp_agnosticd(DEST_CLUSTER_VERSION)
            },
            failFast: false
            utils.teardown_s3_bucket()
          }
          utils.teardown_container_image()
          if (CLEAN_WORKSPACE) {
            cleanWs notFailBuild: true
          }
        }
      }
  }
}
