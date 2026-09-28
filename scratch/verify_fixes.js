const http = require('http');

function request(options, data = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(body);
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

async function verify() {
  console.log('=== VERIFYING USER ISSUE FIXES ===\n');

  // Fix 1: Customer Ingest Usage & XML Download
  console.log('1. Testing Customer Ingest Usage & Download XML...');
  const dlRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/ipdr/simulate-and-download/customer1',
    method: 'POST'
  });
  console.log('   Status:', dlRes.status);
  console.log('   Content-Type:', dlRes.headers['content-type']);
  console.log('   Content-Disposition:', dlRes.headers['content-disposition']);
  console.log('   XML Snippet:\n' + dlRes.raw.substring(0, 200) + '...\n');
  if (dlRes.status !== 200 || !dlRes.raw.includes('<IPDRDoc') || !dlRes.raw.includes('<serviceIdentifier>customer1</serviceIdentifier>')) {
    throw new Error('Customer simulate & download XML failed');
  }

  // Fix 2: Customer Generate Report
  console.log('2. Testing Customer Generate Report (US20)...');
  const rptRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/customer/report?username=customer1&fromDate=2026-09-01&toDate=2026-09-30',
    method: 'GET'
  });
  console.log('   Status:', rptRes.status);
  console.log('   Username:', rptRes.data?.username);
  console.log('   Total Usage:', rptRes.data?.totalUsageGb, 'GB (Upload:', rptRes.data?.totalUploadGb, 'GB, Download:', rptRes.data?.totalDownloadGb, 'GB)');
  console.log('   Daily Usage Points:', rptRes.data?.dailyUsage?.length);
  if (rptRes.status !== 200 || !rptRes.data?.dailyUsage) {
    throw new Error('Customer report generation failed');
  }

  // Also test with empty string dates to ensure graceful fallback:
  const rptDefaultRes = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/customer/report?username=customer1&fromDate=&toDate=',
    method: 'GET'
  });
  console.log('   Report with empty dates status:', rptDefaultRes.status, 'Total GB:', rptDefaultRes.data?.totalUsageGb);
  if (rptDefaultRes.status !== 200) {
    throw new Error('Customer report with default dates failed');
  }

  // Fix 3: Admin Sales Report (US07)
  console.log('\n3. Testing Admin Sales Report (US07)...');
  const adminRpt = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/admin/report',
    method: 'GET'
  });
  console.log('   Status:', adminRpt.status);
  console.log('   Plans in Sales Report:', adminRpt.data?.length);
  for (const p of adminRpt.data || []) {
    console.log(`     - ${p.packageName}: ${p.subscriberCount} subscribers (${p.subscriberPercentage}%), Revenue: $${p.totalRevenueUsd}`);
  }
  if (adminRpt.status !== 200 || !Array.isArray(adminRpt.data) || adminRpt.data.length === 0) {
    throw new Error('Admin sales report failed');
  }

  // Fix 4: Check Operator Dashboard cleanup
  console.log('\n4. Verifying Operator Module cleanup...');
  const opPlans = await request({
    hostname: 'localhost',
    port: 8081,
    path: '/api/operator/plans',
    method: 'GET',
    headers: { 'X-User-Role': 'OPERATOR', 'X-Username': 'operator' }
  });
  console.log('   Operator active plans count:', opPlans.data?.length);
  if (opPlans.status !== 200) {
    throw new Error('Operator plans failed');
  }

  console.log('\n✅ ALL 4 ISSUE VERIFICATIONS PASSED SUCCESSFULLY!');
}

verify().catch(err => {
  console.error('\n❌ Verification Failed:', err);
  process.exit(1);
});
