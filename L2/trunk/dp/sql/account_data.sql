-- ---------------------------
-- Table structure for `account_data`
-- ---------------------------
CREATE TABLE account_data (
  account_name VARCHAR(45) NOT NULL DEFAULT '',
  var  VARCHAR(20) NOT NULL DEFAULT '',
  value VARCHAR(255) ,
  PRIMARY KEY  (account_name,var)
);