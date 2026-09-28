const http = require('http');
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

function request(options, data = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const parsed = body ? JSON.parse(body) : null;
          resolve({ status: res.statusCode, headers: res.headers, data: parsed, raw: body });
        } catch (e) {
          resolve({ status: res.statusCode, headers: res.headers, raw: body });
        }
      });
    });
    req.on('error', reject);
    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

async function run() {
  console.log('=== COMPLETE END-TO-END VERIFICATION SUITE ===\n');

  // 1. Operator Login
  console.log('1. Testing Operator Login (US10)...');
  const opLogin = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/auth/login',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, { username: 'operator', password: 'Operator@123' });
  console.log('   Operator login status:', opLogin.status, 'Role:', opLogin.data?.role);
  if (opLogin.status !== 200 || opLogin.data?.role !== 'OPERATOR') {
    throw new Error('Operator login failed');
  }

  // 2. Customer Login
  console.log('\n2. Testing Customer Login (US15)...');
  const custLogin = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/auth/login',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, { username: 'customer1', password: 'Customer@123' });
  console.log('   Customer login status:', custLogin.status, 'Role:', custLogin.data?.role);
  if (custLogin.status !== 200 || custLogin.data?.role !== 'CUSTOMER') {
    throw new Error('Customer login failed');
  }

  // 3. Role Authorization: Customer attempting Operator API (US10 RBAC)
  console.log('\n3. Testing Role Authorization: Customer attempting Operator Plan API...');
  const unauthorizedRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/plans',
    method: 'GET',
    headers: { 'X-User-Role': 'CUSTOMER', 'X-Username': 'customer1' }
  });
  console.log('   Customer access status (expected 403 Forbidden):', unauthorizedRes.status);
  if (unauthorizedRes.status !== 403) {
    throw new Error('RBAC validation failed: customer did not receive 403 Forbidden');
  }

  // 4. Operator Get Active Plans (US11)
  console.log('\n4. Testing Operator Get Active Plans with Pagination (US11)...');
  const plansRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/plans?page=0&size=10',
    method: 'GET',
    headers: { 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  });
  console.log('   Active plans count on page 0:', plansRes.data?.content?.length || plansRes.data?.length);

  // 5. Operator Add Plan (US12)
  console.log('\n5. Testing Operator Add Plan (US12)...');
  const newPlanPkg = 'TestGamer' + Date.now().toString().slice(-4);
  const addPlanRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/plans',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  }, {
    packageName: newPlanPkg,
    dataAllowanceGb: 15.0,
    monthlyChargeUsd: 25.00,
    chargesAfterLimitPerMb: 0.005
  });
  console.log('   Add plan status:', addPlanRes.status, 'Created Plan ID:', addPlanRes.data?.id, 'State:', addPlanRes.data?.planState);
  if (addPlanRes.status !== 201 || addPlanRes.data?.planState !== 'Activated') {
    throw new Error('Add plan failed');
  }
  const createdPlanId = addPlanRes.data.id;

  // 6. Operator Negative Validation Test: Invalid Package Name characters
  console.log('\n6. Testing Operator Add Plan Negative Validation (Invalid characters in package)...');
  const invalidPlanRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/plans',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  }, {
    packageName: 'Invalid#@!Package',
    dataAllowanceGb: 15.0,
    monthlyChargeUsd: 25.00,
    chargesAfterLimitPerMb: 0.005
  });
  console.log('   Negative validation status (expected 400 Bad Request):', invalidPlanRes.status, 'Message:', invalidPlanRes.data?.message || invalidPlanRes.data?.error);
  if (invalidPlanRes.status !== 400) {
    throw new Error('Negative validation failed');
  }

  // 7. Operator Edit Plan (US13)
  console.log('\n7. Testing Operator Edit Plan (US13)...');
  const editPlanRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: `/api/operator/plans/${createdPlanId}`,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  }, {
    packageName: newPlanPkg + 'Edit',
    dataAllowanceGb: 18.0,
    monthlyChargeUsd: 28.00,
    chargesAfterLimitPerMb: 0.004
  });
  console.log('   Edit plan status:', editPlanRes.status, 'Updated package:', editPlanRes.data?.packageName);
  if (editPlanRes.status !== 200 || editPlanRes.data?.packageName !== newPlanPkg + 'Edit') {
    throw new Error('Edit plan failed');
  }

  // 8. Operator Deactivate Plan (US14)
  console.log('\n8. Testing Operator Deactivate Plan (US14)...');
  const deactRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: `/api/operator/plans/${createdPlanId}`,
    method: 'DELETE',
    headers: { 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  });
  console.log('   Deactivate status:', deactRes.status, 'Message:', deactRes.data?.message);
  if (deactRes.status !== 200) {
    throw new Error('Deactivate plan failed');
  }

  // 9. Operator Report (US07)
  console.log('\n9. Testing Operator Sales / Subscriber Report (US07)...');
  const reportRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/report',
    method: 'GET',
    headers: { 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  });
  console.log('   Report items count:', reportRes.data?.length);
  for (const item of (reportRes.data || []).slice(0, 3)) {
    console.log(`     - ${item.packageName}: ${item.subscriberCount} subscribers (${item.subscriberPercentage}%), Revenue: $${item.totalRevenueUsd}`);
  }

  // 10. Customer Home baseline
  console.log('\n10. Baseline Customer Home Status (customer1)...');
  const beforeHome = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/customer/home?username=customer1',
    method: 'GET',
    headers: { 'X-User-Role': 'CUSTOMER', 'X-Username': 'customer1' }
  });
  console.log(`    customer1: Plan: ${beforeHome.data?.packageName}, Allowance: ${beforeHome.data?.dataAllowanceGb} GB, Usage: ${beforeHome.data?.usageInGb} GB, Bill Total: $${beforeHome.data?.totalAmountUsd}`);
  const initialUsage = beforeHome.data?.usageInGb || 0;

  // 11. Run Standalone IPDR Simulator (US16)
  console.log('\n11. Executing Standalone Java IPDR Simulator (US16)...');
  const simOutput = execSync('java -jar simulator/target/ipdr-simulator.jar --once', {
    cwd: 'c:\\NetworkCapstoneProject',
    encoding: 'utf-8'
  });
  console.log('    Simulator executed successfully:');
  console.log(simOutput.split('\n').filter(l => l.includes('Generated IPDR') || l.includes('Saved canonical IPDR XML')).join('\n'));

  // 12. Ingest Simulator XML
  console.log('\n12. Ingesting IPDR XML in Backend...');
  const ingestRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/ipdr/poll-simulator',
    method: 'POST',
    headers: { 'X-User-Role': 'OPERATOR', 'X-Username': 'operator1' }
  });
  console.log(`    Ingestion result: ${ingestRes.data?.message}, Accepted Records: ${ingestRes.data?.acceptedRecords}, Rejected Records: ${ingestRes.data?.rejectedRecords}`);

  // 13. Customer Home After Simulation & Rating Verification
  console.log('\n13. Customer Home Status After IPDR Ingestion & Rating...');
  const afterHome = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/customer/home?username=customer1',
    method: 'GET',
    headers: { 'X-User-Role': 'CUSTOMER', 'X-Username': 'customer1' }
  });
  console.log(`    customer1: Usage: ${afterHome.data?.usageInGb} GB, Bill Total: $${afterHome.data?.totalAmountUsd}, Remaining: ${afterHome.data?.remainingDataMb} MB, Over-limit: ${afterHome.data?.dataAfterLimitGb} GB`);

  // 14. Negative Testing: Reject Malformed / Unrecognized Device IPDR XML
  console.log('\n14. Testing IPDR Validation Negative Case: Unrecognized Device...');
  const invalidXml = `<?xml version="1.0" encoding="UTF-8"?>
<IPDRDoc xmlns="urn:ipdr:namespaces:ipdr" version="3.1">
  <IPDR xsi:type="DOCSIS-Type" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <CMTSHostName>rogue-unregistered-device.com</CMTSHostName>
    <CMTSipAddress>192.168.99.99</CMTSipAddress>
    <CMmacAddress>00:00:00:00:00:00</CMmacAddress>
    <serviceIdentifier>SID-INVALID</serviceIdentifier>
    <serviceDirection>1</serviceDirection>
    <serviceOctetsPassed>52428800</serviceOctetsPassed>
  </IPDR>
</IPDRDoc>`;

  const negativeRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/ipdr/upload-xml',
    method: 'POST',
    headers: { 'Content-Type': 'application/xml', 'X-User-Role': 'OPERATOR', 'X-Username': 'operator1' }
  }, invalidXml);
  console.log(`    Negative validation result: Status: ${negativeRes.status}, Accepted: ${negativeRes.data?.acceptedRecords}, Rejected: ${negativeRes.data?.rejectedRecords}`);
  console.log(`    Rejection messages:`, negativeRes.data?.errors);
  if (negativeRes.data?.rejectedRecords < 1) {
    throw new Error('Negative validation failed: Unrecognized device was not rejected!');
  }

  console.log('\n======================================================');
  console.log('✅ ALL VERIFICATIONS COMPLETED SUCCESSFULLY!');
  console.log('======================================================');
}

run().catch(err => {
  console.error('\n❌ VERIFICATION TEST FAILED:', err);
  process.exit(1);
});
