locals {
  pnpg_suffix = var.is_pnpg == true ? "-pnpg" : ""
  project     = "${var.prefix}-${var.env_short}"

  monitor_rg_name          = "${local.project}-monitor-rg"
  monitor_appinsights_name = "${local.project}-appinsights"
}