using System;
using System.Collections.Generic;

using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;


class Utility : Form
{  

    protected override void OnLoad(EventArgs e)
    {
      base.OnLoad(e);
      this.FormBorderStyle = FormBorderStyle.None;
    }

    public Utility()
    {
        //this.WindowState = FormWindowState.Minimized;

        var label = new Label();
        label.Text = "Hello from Windows.Forms C# .NET!";
        label.Font = new Font("Roboto", 18);
        label.Location = new Point(30,30);
        label.Size = new Size(500, 60);
        this.Controls.Add(label);

        button1 = new Button();
        button1.Size = new Size(100,50);
        button1.Location = new Point(30,130);
        button1.Text = "Button";
        this.Controls.Add(button1);
        button1.Click += new EventHandler(button1_Click);
    }

    public Button button1;

    private void button1_Click(object sender, EventArgs e)
    {
        //this.WindowState = FormWindowState.Minimized;
        //MessageBox.Show("Hello World");
    }

}  
