const puppeteer = require('puppeteer');
const fs = require('fs');

async function run() {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  
  const sizes = [
    { width: 1920, height: 1080, name: '1920x1080' },
    { width: 1440, height: 900, name: '1440x900' },
    { width: 1366, height: 768, name: '1366x768' },
    { width: 390, height: 844, name: '390x844' },
    { width: 375, height: 812, name: '375x812' }
  ];

  if (!fs.existsSync('screenshots')) {
    fs.mkdirSync('screenshots');
  }

  for (const size of sizes) {
    await page.setViewport({ width: size.width, height: size.height });
    await page.goto('http://localhost:4173/', { waitUntil: 'networkidle0' });
    await page.screenshot({ path: `screenshots/${size.name}_home.png` });
  }

  await browser.close();
}

run().catch(console.error);
