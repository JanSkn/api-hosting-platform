import { test, expect } from '@playwright/test';

test.describe('Dashboard E2E', () => {
  const timestamp = Date.now();
  const testEmail = `e2e_${timestamp}@example.com`;

  test('should allow a user to sign up and view the dashboard', async ({ page }) => {
    await page.goto('/register');

    await page.fill('input[id="name"]', 'E2E Test User');
    await page.fill('input[id="email"]', testEmail);
    await page.fill('input[id="password"]', 'TestPassword123!');

    await page.click('button[type="submit"]');

    await expect(page.locator('h1', { hasText: 'My API Deployments' })).toBeVisible({ timeout: 20000 });

    page.once('dialog', dialog => dialog.accept());
    await page.goto('/settings');
    await page.click('button:has-text("Delete")');
    await expect(page.locator('text=Sign in to your account')).toBeVisible({ timeout: 20000 });
  });
});
