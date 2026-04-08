import { test, expect } from '@playwright/test';

test.describe('Dashboard E2E', () => {
  const timestamp = Date.now();
  const testEmail = `e2e_${timestamp}@example.com`;
  const apiName = `api-${timestamp}`;

  test('should allow a user to sign up and view the dashboard', async ({ page }) => {
    await page.goto('/register');

    await page.fill('input[id="name"]', 'E2E Test User');
    await page.fill('input[id="email"]', testEmail);
    await page.fill('input[id="password"]', 'TestPassword123!');

    await page.click('button[type="submit"]');

    await expect(page.locator('h1', { hasText: 'My API Deployments' })).toBeVisible({ timeout: 10000 });

    page.once('dialog', dialog => dialog.accept());
    await page.goto('/settings');
    await page.click('button:has-text("Delete")');
    await expect(page.locator('text=Sign in to your account')).toBeVisible({ timeout: 10000 });
  });

  test('should allow a user to create a deployment and see it on the dashboard', async ({ page }) => {
    const deployEmail = `deploy_${Date.now()}@example.com`;
    await page.goto('/register');
    await page.fill('input[id="name"]', 'Deploy User');
    await page.fill('input[id="email"]', deployEmail);
    await page.fill('input[id="password"]', 'TestPassword123!');
    await page.click('button[type="submit"]');
    await expect(page.locator('h1', { hasText: 'My API Deployments' })).toBeVisible({ timeout: 10000 });

    await page.click('button:has-text("New API Deployment")', { timeout: 5000 });

    await expect(page.locator('h1', { hasText: 'New Deployment' })).toBeVisible();
    await page.setInputFiles('input#file-upload', 'tests/e2e/sample-api.zip');
    await page.click('button:has-text("Continue")');

    await page.fill('input[id="api-name"]', apiName);

    await page.getByRole('combobox').filter({ hasText: 'Select language' }).click();
    await page.getByRole('option', { name: 'JavaScript' }).click();

    await page.getByRole('combobox').filter({ hasText: 'Select version' }).click();
    await page.getByRole('option', { name: 'Node.js 18.x' }).first().click();

    await page.click('button:has-text("Continue")');

    await expect(page.locator('h2', { hasText: 'Ready to Deploy' })).toBeVisible();
    await page.click('button:has-text("Deploy API")');

    await page.waitForURL(/\/deployment\/.+/, { timeout: 15000 });

    await page.click('button:has-text("Back to Dashboard")');

    await expect(page.locator('h1', { hasText: 'My API Deployments' })).toBeVisible({ timeout: 10000 });
    // Inside ProjectCard the name is in an h3
    await expect(page.locator(`h3:has-text("${apiName}")`).first()).toBeVisible();

    page.once('dialog', dialog => dialog.accept());
    await page.goto('/settings');
    // Ensure we are on settings page
    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible();
    await page.click('button:has-text("Delete")');
    await expect(page.locator('text=Sign in to your account')).toBeVisible({ timeout: 10000 });
  });
});
