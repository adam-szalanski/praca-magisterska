Future plans for this project:
- create a decent README.md
- check if default memory limit didn't interfere with test results
- modify current iteration processing implementation to load file content in batches (to avoid memory limit issues with
  bigger files)
- add data generator (possibly reimplement current python script into java code and integrate with this project)
- add reactor streams comparison
- add spring batch comparison
- design and implement better analysis tool (possibly one that would automatically generate plots into .png files)

Current tests outcome plots:
![](.\doc\wykres_czasy.png)
![](.\doc\wykres_cpu.png)
![](.\doc\wykres_ram.png)